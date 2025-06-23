LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := CTranslate2Proxy

LOCAL_C_INCLUDES :=                                     \
	$(LOCAL_PATH)/../../../../sentencepiece/src/main/jni/sentencepiece/src			\
	$(LOCAL_PATH)/include			\
	$(LOCAL_PATH)/../../../../Tokenizer/src/main/jni/Tokenizer/include			\
	$(LOCAL_PATH)/../../../../CTranslate2/src/main/jni/CTranslate2/include		\

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_SRC_FILES := \
	$(LOCAL_PATH)/src/OpusMtTranslator.cpp \
	$(LOCAL_PATH)/src/M2M100Translator.cpp \
	$(LOCAL_PATH)/src/Small100Translator.cpp \
	$(LOCAL_PATH)/src/Utils.cpp

ifeq ($(APP_OPTIM),debug)
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../CTranslate2/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libctranslate2.so
else
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../CTranslate2/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libctranslate2.so
endif

ifeq ($(APP_OPTIM),debug)
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../sentencepiece/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libsentencepiece.so
else
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../sentencepiece/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libsentencepiece.so
endif

ifeq ($(APP_OPTIM),debug)
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../Tokenizer/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libOpenNMTTokenizer.so
else
	LOCAL_LDLIBS += $(LOCAL_PATH)/../../../../Tokenizer/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/$(TARGET_ARCH_ABI)/libOpenNMTTokenizer.so
endif

LOCAL_CPPFLAGS += -O3 -flto=thin -std=c++20 -fexceptions -frtti
LOCAL_LDFLAGS += -flto=thin -Wl,-plugin-opt=-emulated-tls -fuse-ld=lld
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)
