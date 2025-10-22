#include <jni.h>
#include <string>



#include <stdio.h>

#include "llm_bluelm.h"



#include <fstream>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>
#include <android/log.h>


#include <jni.h>
#include <string>
#include <thread>
#include <chrono>

JavaVM* g_vm = nullptr;

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

class Context {
public:
    JNIEnv* env;
    const jobject* thiz;
    std::string* ans;
};

void llm_eval_cb(const std::string& result, void* data) {
        if(result == "[end of text]"){
            return;
        }
        Context* ctx_ = static_cast<Context *>(data);
        *(ctx_->ans) += result;
        jclass mainActivityClass = ctx_->env->GetObjectClass(*(ctx_->thiz));
        jmethodID mainMethodId = ctx_->env->GetMethodID(mainActivityClass, "updateUI", "(Ljava/lang/String;)V");
        // 调用 updateUI 方法
        jstring jmsg = ctx_->env->NewStringUTF(result.c_str());
        ctx_->env->CallVoidMethod(*(ctx_->thiz), mainMethodId ,  jmsg);
        ctx_->env->DeleteLocalRef(jmsg);
}


using namespace vla;

std::string jstring2String(JNIEnv* env, jstring jStr) {
    const char* cstr = NULL;
    jboolean isCopy = false;
    cstr = env->GetStringUTFChars(jStr, &isCopy);
    if (cstr == nullptr) {
        env->ReleaseStringUTFChars(jStr, cstr);
        return "";
    }
    std::string str = std::string(cstr);
    return str;
}


extern "C" JNIEXPORT jlong JNICALL
Java_com_lanqiDoctor_demo_ui_activity_LocalLLMActivity_init(
        JNIEnv* env, jobject thiz ) {

    llm_bluelm *handle = new llm_bluelm();

    if(handle->init() != vla::LLM_CODE::LLM_SUCCESS){
        handle->release();
        handle = 0;
    }
    return reinterpret_cast<jlong>(handle);
}

extern "C" JNIEXPORT void JNICALL
Java_com_lanqiDoctor_demo_ui_activity_LocalLLMActivity_reset(
        JNIEnv* env,
        jobject /* this */, jlong handle) {
    llm_bluelm* llm_handle = nullptr;
    if(handle != 0){
        llm_handle = reinterpret_cast<llm_bluelm*>(handle);
    }
    if (llm_handle == nullptr) return;
    llm_handle->reset();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lanqiDoctor_demo_ui_activity_LocalLLMActivity_release(
        JNIEnv* env,
        jobject /* this */, jlong handle) {
    llm_bluelm* llm_handle = nullptr;
    if(handle != 0){
        llm_handle = reinterpret_cast<llm_bluelm*>(handle);
    }
    if (llm_handle == nullptr) return;
    delete llm_handle;
    llm_handle = nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_lanqiDoctor_demo_ui_activity_LocalLLMActivity_forward(
        JNIEnv* env,
        jobject thiz, jlong handle,jstring prompt_) {
    llm_bluelm* llm_handle = nullptr;
    if(handle != 0){
        llm_handle = reinterpret_cast<llm_bluelm*>(handle);
    }

    if(llm_handle == nullptr){
        return;
    }
    std::string prompt = jstring2String(env, prompt_);
    if (prompt.empty()) {
        return;
    }

    std::string new_prompt = llm_handle->build_prompt(prompt);
    jobject globalThiz = env->NewGlobalRef(thiz);

    std::thread([llm_handle,new_prompt, globalThiz]() {
        JNIEnv* env;
        g_vm->AttachCurrentThread(&env, nullptr);
        Context ctx;
        std::string ans;
        ctx.env = env;
        ctx.thiz = &globalThiz;
        ctx.ans = &ans;

        llm_handle->forward(new_prompt,llm_eval_cb,&ctx);

        llm_handle->set_history(ans);

        env->DeleteGlobalRef(globalThiz);
        g_vm->DetachCurrentThread();

    }).detach();


    return;
}
