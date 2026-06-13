# Regras básicas para Compose e KMP
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlin.coroutines.** { *; }

# Firebase Firestore / Auth
-keep class com.google.firebase.** { *; }
-keep class dev.gitlive.firebase.** { *; }
-keepattributes InnerClasses

# Ktor
-keep class io.ktor.** { *; }

# Serialization
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }
