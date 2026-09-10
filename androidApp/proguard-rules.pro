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

# WorkManager (transitive dependency, pulled in by Firebase - not declared directly anywhere in
# this project) crashes at process start under R8 minification: "Failed to create an instance of
# androidx.work.impl.WorkDatabase" from androidx.startup.InitializationProvider, before even
# Application.onCreate runs. R8 strips Room's generated WorkDatabase implementation class unless
# explicitly kept. Reproduced only in a minified release build - assembleDebug/testDebugUnitTest
# never exercise R8, which is why this stayed invisible through the whole AGP 9 migration.
-keep class androidx.work.impl.WorkDatabase
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Suprimir avisos de classes ausentes (R8)
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.debug.**
-dontwarn javax.naming.**
