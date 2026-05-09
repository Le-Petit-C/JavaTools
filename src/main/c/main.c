#include <jni.h>

// 缓存字段

static jfieldID arrayListElementDataField;
static jfieldID arrayListSizeField;
static jfieldID arrayListModCountField;

JNIEXPORT void JNICALL
Java_lpc_javaTools_NativeLoader_nativeInitialize(JNIEnv* env, jclass cls) {
    jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    arrayListElementDataField = (*env)->GetFieldID(env, arrayListClass, "elementData", "[Ljava/lang/Object;");
    arrayListSizeField = (*env)->GetFieldID(env, arrayListClass, "size", "I");
    arrayListModCountField = (*env)->GetFieldID(env, arrayListClass, "modCount", "I");
}

 JNIEXPORT jobjectArray JNICALL
 Java_lpc_javaTools_utils_algorithm_IterateUtils_getArrayListElementData(JNIEnv* env, jclass cls, jobject list) {
     return (*env)->GetObjectField(env, list, arrayListElementDataField);
 }

JNIEXPORT void JNICALL
Java_lpc_javaTools_utils_algorithm_IterateUtils_setArrayListSize(JNIEnv* env, jclass cls, jobject list, jint size) {
     (*env)->SetIntField(env, list, arrayListSizeField, size);
}

JNIEXPORT void JNICALL
Java_lpc_javaTools_utils_algorithm_IterateUtils_increaseArrayListModCount(JNIEnv* env, jclass cls, jobject list) {
    (*env)->SetIntField(env, list, arrayListModCountField, (*env)->GetIntField(env, list, arrayListModCountField));
}