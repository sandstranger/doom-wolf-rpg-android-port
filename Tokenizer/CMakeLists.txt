cmake_minimum_required(VERSION 3.22.1)
project(Tokenizer)

add_link_options("LINKER:--build-id=none")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -llog")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -llog")

set(ROOT_DIR ${CMAKE_SOURCE_DIR}/..)

if (CMAKE_BUILD_TYPE EQUAL "DEBUG")
    set(ICU_LIBS_DIR ${ROOT_DIR}/icu4c/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/${ANDROID_ABI})
    set(ICU_LIBRARIES ${ICU_LIBS_DIR}/libicudata.so ${ICU_LIBS_DIR}/libicui18n.so ${ICU_LIBS_DIR}/libicuuc.so)
else()
    set(ICU_LIBS_DIR ${ROOT_DIR}/icu4c/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/${ANDROID_ABI})
    set(ICU_LIBRARIES ${ICU_LIBS_DIR}/libicudata.so ${ICU_LIBS_DIR}/libicui18n.so ${ICU_LIBS_DIR}/libicuuc.so)
endif ()

set(ICU_INCLUDE_DIRS "${ROOT_DIR}/icu4c/src/main/jni/icu4c_android/i18n")
include_directories(PUBLIC "${ROOT_DIR}/icu4c/src/main/jni/icu4c_android/i18n")
include_directories(PUBLIC "${ROOT_DIR}/icu4c/src/main/jni/icu4c_android/common")

add_subdirectory("${CMAKE_SOURCE_DIR}/src/main/jni/Tokenizer")
