# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("lanqiDoctor")

set(JNILIB_DIR "${CMAKE_CURRENT_SOURCE_DIR}/libs")

# ================================
# AIGC LLM 相关配置 (暂时注释掉)
# ================================
# add_library( # Sets the name of the library.
#         llm_base_7b
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(llm_base_7b PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libllm_base_7b.so)

# add_library( # Sets the name of the library.
#         llm_apu_7b
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(llm_apu_7b PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libllm_apu_7b.so)

# add_library( # Sets the name of the library.
#         llm_apu
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(llm_apu PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libllm_apu.so)

# add_library( # Sets the name of the library.
#         vivo_tokenizer
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(vivo_tokenizer PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libvivo_tokenizer.so)

# add_library( # Sets the name of the library.
#         core_tokenizers
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(core_tokenizers PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libcore_tokenizers.so)

# add_library( # Sets the name of the library.
#         c++
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(c++ PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libc++.so)

# add_library( # Sets the name of the library.
#         dmabuf
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         IMPORTED)
# set_target_properties(dmabuf PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/libdmabufheap.so)

# ================================
# OCR 相关配置
# ================================

# 设置 PaddleLite 和 OpenCV 路径
set(PaddleLite_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../PaddleLite")
include_directories(${PaddleLite_DIR}/cxx/include)

set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../OpenCV/sdk/native/jni")
find_package(OpenCV REQUIRED)
message(STATUS "OpenCV libraries: ${OpenCV_LIBS}")
include_directories(${OpenCV_INCLUDE_DIRS})

# 设置编译选项
set(CMAKE_CXX_FLAGS
    "${CMAKE_CXX_FLAGS} -ffast-math -Ofast -Os -DNDEBUG -fno-exceptions -fomit-frame-pointer -fno-asynchronous-unwind-tables -fno-unwind-tables"
)
set(CMAKE_CXX_FLAGS
    "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -fdata-sections -ffunction-sections -fexceptions"
)
set(CMAKE_SHARED_LINKER_FLAGS
    "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections -Wl,-z,nocopyreloc")

# 添加 PaddleLite 库
add_library(
  paddle_light_api_shared
  SHARED
  IMPORTED)

set_target_properties(
  paddle_light_api_shared
  PROPERTIES
    IMPORTED_LOCATION
    ${PaddleLite_DIR}/cxx/libs/${ANDROID_ABI}/libpaddle_light_api_shared.so
)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# OCR Native 库
add_library(
  Native
  SHARED
  Native.cc pipeline.cc utils.cc cls_process.cc rec_process.cc det_process.cc db_post_process.cc clipper.cpp)

# AIGC 库 (暂时注释掉)
# add_library( # Sets the name of the library.
#         aigc
#         # Sets the library as a shared library.
#         SHARED
#         # Provides a relative path to your source file(s).
#         llm_bluelm.cpp
#         native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# OCR 库链接
target_link_libraries(
  Native
  paddle_light_api_shared
  ${OpenCV_LIBS}
  GLESv2
  EGL
  jnigraphics
  ${log-lib})

# AIGC 库链接 (暂时注释掉)
# target_link_libraries( # Specifies the target library.
#         aigc
#         llm_base_7b
#         llm_apu_7b
#         c++
#         dmabuf
#         vivo_tokenizer
#         core_tokenizers
#         # Links the target library to the log library
#         # included in the NDK.
#         ${log-lib})