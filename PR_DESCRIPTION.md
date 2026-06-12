Resumo das alterações para o PR

O problema
- A aplicação falhava em tempo de execução com java.lang.ClassNotFoundException para
  com.lpstudio.bolaodagalera.MainActivity. Isso normalmente indica que a classe não foi
  empacotada no APK (build incompleto/erro de dependências) ou que um APK antigo estava
  sendo instalado.

O que foi alterado
- `composeApp/build.gradle.kts`
  - Adicionado Firebase BOM ao bloco `dependencies` do módulo:
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
  - Substituído o shorthand de `material-icons-extended` por dependência explícita com
    versão: implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
  - Removido o uso de `platform(...)` dentro do bloco `kotlin { sourceSets { ... } }`
    (isso pode causar erro de compilação do Kotlin Gradle DSL).

Resultados dos testes locais
- ./gradlew :composeApp:clean :composeApp:assembleDebug -> build completou com sucesso
- APK gerado: composeApp/build/outputs/apk/debug/composeApp-debug.apk
- Verifiquei que a classe MainActivity foi compilada e está presente nos .dex e em
  composeApp/build/tmp/kotlin-classes/debug/com/lpstudio/bolaodagalera/MainActivity.class
- Instalei e iniciei o APK no emulador (adb install -r ... && adb shell am start -n ...)
  -> não houve mais o ClassNotFoundException durante a inicialização.

Recomendações ao aceitar o PR
1. Teste no seu dispositivo físico e em outras APIs Android (especialmente se usar
   WebView / bibliotecas nativas):

   ```bash
   ./gradlew :composeApp:clean :composeApp:assembleDebug
   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
   adb logcat -c
   adb shell am start -n com.lpstudio.bolaodagalera/.MainActivity
   adb logcat --pid=$(adb shell pidof -s com.lpstudio.bolaodagalera) | sed -n '1,200p'
   ```

2. Confirme que o `google-services.json` está no lugar correto (normalmente em
   `composeApp/src/androidMain/resources/` ou no módulo Android) e que o plugin
   `com.google.gms.google-services` está aplicado onde necessário.

3. Se for subir builds de release com minify/enabling R8, adicione regras para manter
   activities e classes Kotlin geradas por Compose (se observar crash somente em release).

Como criar o commit/PR localmente (exemplo):

```bash
git checkout -b fix/firebase-bom-composeapp
git add composeApp/build.gradle.kts
git commit -m "fix(composeApp): add Firebase BOM and pin material-icons-extended"
git push --set-upstream origin fix/firebase-bom-composeapp
# Abra um Pull Request na interface do GitHub/GitLab apontando a branch para main/master
```

Observações
- O BOM foi definido para `32.2.0` no momento das alterações; o Gradle pode resolver
  para versões mais novas (ex.: 33.x). Se desejar, posso atualizar o BOM para uma
  versão fixa mais nova antes de abrir o PR.

Se quiser, abro o PR por você (se me fornecer acesso remoto/permite usar suas credenciais),
ou apenas crio o patch/branch e te envio as instruções — o que prefere?
