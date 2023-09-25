/*
 * rtl_tcp_andro is a library that uses libusb and librtlsdr to
 * turn your Realtek RTL2832 based DVB dongle into a SDR receiver.
 * It independently implements the rtl-tcp API protocol for native Android usage.
 * Copyright (C) 2022 by Signalware Ltd <driver@sdrtouch.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <jni.h>
#include <stdlib.h>
#include "common.h"
#include "rtl-sdr-android.h"
#include "SdrException.h"
#include <string.h>
#include <math.h>

#define RUN_OR(command, exit_command) { \
    int cmd_result = command; \
    if (cmd_result != 0) { \
        throwExceptionWithInt(env, "com/sdrtouch/core/exceptions/SdrException", cmd_result); \
        exit_command; \
    }; \
}

#define RUN_OR_GOTO(command, label) RUN_OR(command, goto label);

/// a structure which holds the data necessary to identify the underlying device. This structure is
/// handled to java and sent back and forth for most of the calls.
typedef struct rtlsdr_android {
    rtlsdr_dev_t *rtl_dev;
    jclass instance;
    /// if not zero, the packets will be trimmed by all items which are not exceeding the amplitude specified by this margin.
    /// no packet will be sent if the whole packet does not exceed the specified amplitude
    int margin;
    u_int8_t *maglut;
} rtlsdr_android_t;

void send_to_java(rtlsdr_android_t *dev, unsigned char *buf, uint32_t len, void *pointer);

#define WITH_DEV(x) rtlsdr_android_t* x = (rtlsdr_android_t*) pointer

void initialize(JNIEnv *env) {
    LOGI_NATIVE("Initializing");
}

static int set_gain_by_index(rtlsdr_dev_t *_dev, unsigned int index) {
    int res = 0;
    int *gains;
    int count = rtlsdr_get_tuner_gains(_dev, NULL);

    if (count > 0 && (unsigned int) count > index) {
        gains = malloc(sizeof(int) * count);
        rtlsdr_get_tuner_gains(_dev, gains);

        res = rtlsdr_set_tuner_gain(_dev, gains[index]);

        free(gains);
    }

    return res;
}

static int set_gain_by_perc(rtlsdr_dev_t *_dev, unsigned int percent) {
    int res = 0;
    int *gains;
    int count = rtlsdr_get_tuner_gains(_dev, NULL);
    unsigned int index = (percent * count) / 100;
    if (index < 0) index = 0;
    if (index >= (unsigned int) count) index = (unsigned int) (count - 1);

    gains = malloc(sizeof(int) * count);
    rtlsdr_get_tuner_gains(_dev, gains);

    res = rtlsdr_set_tuner_gain(_dev, gains[index]);

    free(gains);

    return res;
}

// Each I and Q value varies from 0 to 255. To get from the
// unsigned (0-255) range you therefore subtract 127 from each I and Q, giving you
// a range from -127 to +128.
//
// We want to improve things by subtracting 127.5, Well in integer arithmatic we can't
// subtract half, so, we'll double everything up, and then compensate for the doubling
// in the multiplier at the end.
//
// To decode the AM signal, you need the magnitude of the waveform, which is given by sqrt((I^2)+(Q^2))
// The most this could be is if I&Q are both 128 (255 after doubling), so you could end up with a magnitude
// of 360.624458.
//
// However, in reality the magnitude of the signal should never exceed the range -1 to +1, because the
// values are I = rCos(w) and Q = rSin(w). Therefore the integer computed magnitude should (can?) never
// exceed 128. Therefore we will oversaturate the magnitude a bit.
//
// If we scale up the results so that they range from 0 to 255 then we need to multiply
// by 0.7071. Since the lowest number of i/q is 1 we will substract by sqrt(1^2+1^2)
//
void prepare_amplitude_calculation(jlong pointer) {
    WITH_DEV(dev);
    dev->maglut = (uint8_t *) malloc(256 * 256);
    for (int i = 0; i <= 255; i++) {
        for (int q = 0; q <= 255; q++) {

            int mag_i = (i * 2) - 255;
            int mag_q = (q * 2) - 255;

            int mag = (int) round(
                    (sqrt((mag_i * mag_i) + (mag_q * mag_q)) * 0.71) - 1.4142);
            dev->maglut[(i * 256) + q] = (uint8_t) (mag <= 255 ? mag : 255);
//            if (i < 10 || i > 250 || (i > 125 && i < 130))
//                if (q < 10 || q > 250 || (q > 125 && q < 130))
//                    LOGI("i %d, q %d, mag %d", i, q, dev->maglut[(i * 256) + q]);
        }
    }

}

/// called whenever data are received from the stick. It will call dataRevceived from java in turn.
void rtlsdr_callback(unsigned char *buf, uint32_t len, void *pointer) {
    WITH_DEV(dev);
    if (dev->rtl_dev == NULL) return;

    if (dev->maglut != NULL) {
        // calculate the amplitudes
        unsigned char *buf2 = malloc(len / 2);
        unsigned char *m = buf2;
        for (int i = 0; i < len; i += 2) {
            *m++ = (dev->maglut[buf[i] * 256 + buf[i + 1]]);
        }
        len = len / 2;
        if (dev->margin > 0) {
            // User wants to trim the amplitudes by margin, remove them now
            uint32_t firstIndex = 0;
            int found = 0;
            for (uint32_t i = 0; i < len; ++i) {
                if (buf2[i] > dev->margin) {
                    firstIndex = i;
                    found = 1;
                    break;
                }
            }
            if (found) {
                // if the loop does not find any amplitude exceeding the margin we take the firstIndex IQ tuple only.
                uint32_t lastIndex = firstIndex;
                for (uint32_t i = len - 1; i > firstIndex; --i) {
                    if (buf2[i] > dev->margin) {
                        lastIndex = i;
                        break;
                    }
                }
                // send remaining items towards flutter
                len = lastIndex - firstIndex + 1;
                send_to_java(dev, buf2 + firstIndex, len, pointer);
                free(buf2);
                return;
            } else {
                // no relevant data, do not send it
                free(buf2);
                return;
            }
        }
        send_to_java(dev, buf2, len, pointer);
        free(buf2);
        return;
    }

    if (dev->margin == 0) {
        // raw I/Q data
        send_to_java(dev, buf, len, pointer);
        return;
    }

    uint32_t firstIndex = 0;
    int found = 0;
    for (uint32_t i = 0; i < len; i += 2) {
        if (buf[i] < 127 - dev->margin || buf[i] > 127 + dev->margin) {
            firstIndex = i;
            found = 1;
            break;
        }
        if (buf[i + 1] < 127 - dev->margin || buf[i + 1] > 127 + dev->margin) {
            firstIndex = i;
            found = 1;
            break;
        }
    }
    if (found) {
        // if the loop does not find any amplitude exceeding the margin we take the firstIndex IQ tuple only.
        uint32_t lastIndex = firstIndex;
        for (uint32_t i = len - 2; i > firstIndex; i -= 2) {
            if (buf[i] < 127 - dev->margin || buf[i] > 127 + dev->margin) {
                lastIndex = i;
                break;
            }
            if (buf[i + 1] < 127 - dev->margin || buf[i + 1] > 127 + dev->margin) {
                lastIndex = i;
                break;
            }
        }
        // send remaining items towards flutter
        len = lastIndex - firstIndex + 2;
        send_to_java(dev, buf + firstIndex, len, pointer);
        return;
    } else {
        // no relevant data, do not send it
        return;
    }

}

void send_to_java(rtlsdr_android_t *dev, unsigned char *buf, uint32_t len, void *pointer) {
    JNIEnv *env;
    int res = attachThread(&env);
    jbyteArray jData = (*env)->NewByteArray(env, (jsize) len);
    if (!jData) return;
    (*env)->SetByteArrayRegion(env, jData, 0, (jsize) len, (jbyte *) buf);

    jclass clazz = (*env)->GetObjectClass(env, dev->instance);
    jmethodID dataRead = (*env)->GetMethodID(env, clazz, "dataReceived", "([BI)V");

    (*env)->CallVoidMethod(env, dev->instance, dataRead, jData, (jint) len);
    (*env)->DeleteLocalRef(env, jData);
    detatchThread(res);
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_openAsync(JNIEnv *env, jobject thiz, jlong pointer,
                                                       jint fd, jint gain, jlong samplingrate,
                                                       jlong frequency, jint ppm,
                                                       jstring device_path) {
    WITH_DEV(dev);
    const char *devicePath = (*env)->GetStringUTFChars(env, device_path, 0);

    rtlsdr_dev_t *device = NULL;
    RUN_OR_GOTO(rtlsdr_open2(&device, fd, devicePath), rel_jni);

    if (ppm != 0) {
        if (rtlsdr_set_freq_correction(device, ppm) < 0) {
            LOGI("WARNING: Failed to set ppm to %d", ppm);
        }
    }

    int result = 0;
    if (samplingrate < 0 ||
        (result = rtlsdr_set_sample_rate(device, (uint32_t) samplingrate)) < 0) {
        LOGI("ERROR: Failed to set sample rate to %lld", samplingrate);
        // LIBUSB_ERROR_IO is -1
        // LIBUSB_ERROR_TIMEOUT is -7
        if (result == -1 || result == -7) {
            RUN_OR(EXIT_NOT_ENOUGH_POWER, goto err);
        } else {
            RUN_OR(EXIT_WRONG_ARGS, goto err);
        }
    } else {
        LOGI("Set sampling rate to %lld", samplingrate);
    }

    if (frequency < 0 || rtlsdr_set_center_freq(device, (uint32_t) frequency) < 0) {
        LOGI("ERROR: Failed to frequency to %lld", frequency);
        RUN_OR(EXIT_WRONG_ARGS, goto err);
    }

    if (0 == gain) {
        if (rtlsdr_set_tuner_gain_mode(device, 0) < 0)
            LOGI("WARNING: Failed to enable automatic gain");
    } else {
        /* Enable manual gain */
        if (rtlsdr_set_tuner_gain_mode(device, 1) < 0)
            LOGI("WARNING: Failed to enable manual gain");

        if (rtlsdr_set_tuner_gain(device, gain) < 0)
            LOGI("WARNING: Failed to set tuner gain");
        else
            LOGI("Tuner gain set to %f dB", gain / 10.0);
    }

    if (rtlsdr_reset_buffer(device) < 0)
        LOGI("WARNING: Failed to reset buffers");

    dev->rtl_dev = device;

    int succesful = 1;
    EXCEPT_SAFE_NUM(jclass clazz = (*env)->GetObjectClass(env, thiz));
    EXCEPT_SAFE_NUM(
            jmethodID announceOnOpen = (*env)->GetMethodID(env, clazz, "announceOnOpen", "()V"));
    EXCEPT_DO((*env)->CallVoidMethod(env, thiz, announceOnOpen), succesful = 0);
    if (rtlsdr_read_async(device, rtlsdr_callback, (void *) dev, 0, 0)) {
        LOGI("rtlsdr_read_async failed");
        succesful = 0;
    } else
        LOGI("rtlsdr_read_async finished successfully");

    /// will be called from java anyway
//    EXCEPT_SAFE_NUM(
//            jmethodID announceOnClose = (*env)->GetMethodID(env, clazz, "announceOnClose", "()V"));
//    EXCEPT_DO((*env)->CallVoidMethod(env, thiz, announceOnClose), succesful = 0);

    dev->rtl_dev = NULL;
    rtlsdr_close(device);

    (*env)->ReleaseStringUTFChars(env, device_path, devicePath);

    return succesful ? ((jboolean) JNI_TRUE) : ((jboolean) JNI_FALSE);

    err:
    rtlsdr_close(device);

    rel_jni:
    (*env)->ReleaseStringUTFChars(env, device_path, devicePath);

    return (jboolean) JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_initialize(JNIEnv *env, jobject instance) {

    rtlsdr_android_t *ptr = malloc(sizeof(rtlsdr_android_t));
    ptr->rtl_dev = NULL;
    ptr->instance = (*env)->NewGlobalRef(env, instance);
    ptr->margin = 0;
    ptr->maglut = NULL;
    //prepare_amplitude_calculation(ptr);
    return (jlong) ptr;
}

JNIEXPORT void JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_dispose(JNIEnv *env, jobject instance, jlong pointer) {
    WITH_DEV(dev);
    if (dev->rtl_dev != NULL) {
        rtlsdr_close(dev->rtl_dev);
        dev->rtl_dev = NULL;
    }
    if (dev->maglut != NULL) {
        free(dev->maglut);
    }
    (*env)->DeleteGlobalRef(env, dev->instance);
    free((void *) dev);
}

JNIEXPORT void JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_close__J(JNIEnv *env, jobject instance,
                                                      jlong pointer) {
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setFrequency(JNIEnv *env, jobject instance,
                                                          jlong pointer, jlong frequency) {
    WITH_DEV(dev);
    if (dev->rtl_dev == NULL) return (jboolean) JNI_FALSE;;

    if (frequency < 0 || rtlsdr_set_center_freq(dev->rtl_dev, (uint32_t) frequency) < 0) {
        LOGI("ERROR: Failed to frequency to %lld", frequency);
        RUN_OR(EXIT_WRONG_ARGS, goto err);
    }
    return (jboolean) JNI_TRUE;

    err:
    return (jboolean) JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setSamplingrate(__attribute__((unused)) JNIEnv *env,
                                                             __attribute__((unused)) jobject thiz,
                                                             jlong pointer,
                                                             jlong samplingrate) {
    WITH_DEV(dev);
    int result = 0;
    if (samplingrate < 0 ||
        (result = rtlsdr_set_sample_rate(dev->rtl_dev, (uint32_t) samplingrate)) < 0) {
        LOGI("ERROR: Failed to set sample rate to %lld", samplingrate);
        // LIBUSB_ERROR_IO is -1
        // LIBUSB_ERROR_TIMEOUT is -7
        if (result == -1 || result == -7) {
            RUN_OR(EXIT_NOT_ENOUGH_POWER, goto err);
        } else {
            RUN_OR(EXIT_WRONG_ARGS, goto err);
        }
    } else {
        LOGI("Set sampling rate to %lld", samplingrate);
    }
    return (jboolean) JNI_TRUE;

    err:
    return (jboolean) JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setTunergainMode(__attribute__((unused)) JNIEnv *env,
                                                              __attribute__((unused)) jobject thiz,
                                                              jlong pointer,
                                                              jint gain) {
    WITH_DEV(dev);
    if (0 == gain) {
        if (rtlsdr_set_tuner_gain_mode(dev->rtl_dev, 0) < 0) {
            LOGI("WARNING: Failed to enable automatic gain");
            return (jboolean) JNI_FALSE;
        }
    } else {
        /* Enable manual gain */
        if (rtlsdr_set_tuner_gain_mode(dev->rtl_dev, 1) < 0) {
            LOGI("WARNING: Failed to enable manual gain");
            return (jboolean) JNI_FALSE;
        }

        if (rtlsdr_set_tuner_gain(dev->rtl_dev, gain) < 0) {
            LOGI("WARNING: Failed to set tuner gain");
            return (jboolean) JNI_FALSE;
        } else
            LOGI("Tuner gain set to %f dB", gain / 10.0);
    }
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setFrequencyCorrection(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jlong pointer, jint ppm) {
    WITH_DEV(dev);
    if (ppm != 0) {
        if (rtlsdr_set_freq_correction(dev->rtl_dev, ppm) < 0) {
            LOGI("WARNING: Failed to set ppm to %d", ppm);
            return (jboolean) JNI_FALSE;
        }
        return (jboolean) JNI_TRUE;
    }
    return (jboolean) JNI_FALSE;

}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setTestmode(__attribute__((unused)) JNIEnv *env,
                                                         __attribute__((unused)) jobject thiz,
                                                         jlong pointer,
                                                         jint testmode) {
    WITH_DEV(dev);
    rtlsdr_set_testmode(dev->rtl_dev, testmode);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setAgcMode(__attribute__((unused)) JNIEnv *env,
                                                        __attribute__((unused)) jobject thiz,
                                                        jlong pointer,
                                                        jint agcmode) {
    WITH_DEV(dev);
    rtlsdr_set_agc_mode(dev->rtl_dev, agcmode);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setDirectSampling(__attribute__((unused)) JNIEnv *env,
                                                               __attribute__((unused)) jobject thiz,
                                                               jlong pointer, jint directsampling) {
    WITH_DEV(dev);
    rtlsdr_set_direct_sampling(dev->rtl_dev, directsampling);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setOffsetTuning(__attribute__((unused)) JNIEnv *env,
                                                             __attribute__((unused)) jobject thiz,
                                                             jlong pointer, jint on) {
    WITH_DEV(dev);
    rtlsdr_set_offset_tuning(dev->rtl_dev, on);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setRtlXtalFreq(__attribute__((unused)) JNIEnv *env,
                                                            __attribute__((unused)) jobject thiz,
                                                            jlong pointer, jlong frequency) {
    WITH_DEV(dev);
    rtlsdr_set_xtal_freq(dev->rtl_dev, frequency, 0);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setTunerXtalFreq(__attribute__((unused)) JNIEnv *env,
                                                              __attribute__((unused)) jobject thiz,
                                                              jlong pointer, jlong frequency) {
    WITH_DEV(dev);
    rtlsdr_set_xtal_freq(dev->rtl_dev, 0, frequency);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setTunerGainByIndex(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jlong pointer, jint index) {
    WITH_DEV(dev);
    set_gain_by_index(dev->rtl_dev, index);
    return (jboolean) JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setTunergainByPercentage(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jlong pointer, jint percentage) {
    WITH_DEV(dev);
    set_gain_by_perc(dev->rtl_dev, percentage);
    return (jboolean) JNI_TRUE;
}

//case TCP_SET_IF_TUNER_GAIN:
//rtlsdr_set_tuner_if_gain(dev->rtl_dev, cmd->parameter >> 16,
//(short) (cmd->parameter & 0xffff));
//break;
//case TCP_ANDROID_EXIT:
//LOGI("tcpCommandCallback: client requested to close rtl_tcp_andro");
//sdrtcp_stop_serving_client(tcpserv);
//break;

JNIEXPORT jlong JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getRtlXtalFreq(__attribute__((unused)) JNIEnv *env,
                                                            __attribute__((unused)) jobject thiz,
                                                            jlong pointer) {
    WITH_DEV(dev);
    uint32_t rtl_freq;
    uint32_t tuner_freq;
    rtlsdr_get_xtal_freq(dev->rtl_dev, &rtl_freq, &tuner_freq);
    return rtl_freq;
}

JNIEXPORT jlong JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getTunerXtalFreq(__attribute__((unused)) JNIEnv *env,
                                                              __attribute__((unused)) jobject thiz,
                                                              jlong pointer) {
    WITH_DEV(dev);
    uint32_t rtl_freq;
    uint32_t tuner_freq;
    rtlsdr_get_xtal_freq(dev->rtl_dev, &rtl_freq, &tuner_freq);
    return tuner_freq;
}

JNIEXPORT jstring JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getManufacturer(__attribute__((unused)) JNIEnv *env,
                                                             __attribute__((unused)) jobject thiz,
                                                             jlong pointer) {
    WITH_DEV(dev);
    char manufacturer[256];
    char product[256];
    char serial[256];
    /*uint32_t result =*/ rtlsdr_get_usb_strings(dev->rtl_dev, manufacturer, product, serial);
    return (*env)->NewStringUTF(env, manufacturer);
}

JNIEXPORT jstring JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getProduct(__attribute__((unused)) JNIEnv *env,
                                                        __attribute__((unused)) jobject thiz,
                                                        jlong pointer) {
    WITH_DEV(dev);
    char manufacturer[256];
    char product[256];
    char serial[256];
    /*uint32_t result =*/ rtlsdr_get_usb_strings(dev->rtl_dev, manufacturer, product, serial);
    return (*env)->NewStringUTF(env, product);
}

JNIEXPORT jstring JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getSerial(__attribute__((unused)) JNIEnv *env,
                                                       __attribute__((unused)) jobject thiz,
                                                       jlong pointer) {
    WITH_DEV(dev);
    char manufacturer[256];
    char product[256];
    char serial[256];
    /*uint32_t result =*/ rtlsdr_get_usb_strings(dev->rtl_dev, manufacturer, product, serial);
    return (*env)->NewStringUTF(env, serial);
}

JNIEXPORT jlong JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getFrequency(__attribute__((unused)) JNIEnv *env,
                                                          __attribute__((unused)) jobject thiz,
                                                          jlong pointer) {
    WITH_DEV(dev);
    return rtlsdr_get_center_freq(dev->rtl_dev);
}

JNIEXPORT jint JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getFrequencyCorrection(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jlong pointer) {
    WITH_DEV(dev);
    return rtlsdr_get_freq_correction(dev->rtl_dev);
}

// rtlsdr_get_tuner_type

// rtlsdr_get_tuner_gains


JNIEXPORT jint JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getTunergain(__attribute__((unused)) JNIEnv *env,
                                                          __attribute__((unused)) jobject thiz,
                                                          jlong pointer) {
    WITH_DEV(dev);
    return rtlsdr_get_tuner_gain(dev->rtl_dev);
}

JNIEXPORT jlong JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getSamplingrate(__attribute__((unused)) JNIEnv *env,
                                                             __attribute__((unused)) jobject thiz,
                                                             jlong pointer) {
    WITH_DEV(dev);
    return rtlsdr_get_sample_rate(dev->rtl_dev);
}


JNIEXPORT jint JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_getMargin(__attribute__((unused)) JNIEnv *env,
                                                       __attribute__((unused)) jobject thiz,
                                                       jlong pointer) {
    WITH_DEV(dev);
    return dev->margin;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setMargin(__attribute__((unused)) JNIEnv *env,
                                                       __attribute__((unused)) jobject thiz,
                                                       jlong pointer,
                                                       jint margin) {
    WITH_DEV(dev);
    dev->margin = margin;
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_sdrtouch_rtlsdr_driver_RtlSdrDevice_setAmplitude(JNIEnv *env, jobject thiz, jlong pointer, jint on) {
    WITH_DEV(dev);
    if (on) {
        if (dev->maglut)
            return JNI_FALSE;
        prepare_amplitude_calculation(pointer);
        return JNI_TRUE;
    } else {
        if (dev->maglut) {
            free(dev->maglut);
            dev->maglut = NULL;
            return JNI_TRUE;
        }
        return JNI_FALSE;
    }
}