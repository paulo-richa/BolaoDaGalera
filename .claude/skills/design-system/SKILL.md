---
name: design-system
description: Use sempre que mexer em código UI/Compose no BolaoDaGalera — criando, editando ou revisando qualquer tela, componente, token de tema ou string. Aplica as regras de design system do app (encapsulamento do módulo :designsystem, zero import de material3 em telas, zero string hardcoded).
---

# Design System do BolaoDaGalera

Este app está sendo modularizado num módulo Gradle dedicado `:designsystem`
(KMP: `commonMain`/`androidMain`/`iosMain`). As regras abaixo não são
preferências de estilo opcionais — são o contrato de arquitetura real
desta base de código. Aplique-as sempre que escrever ou tocar um arquivo
`.kt` dentro de `presentation/` (ou o próprio módulo design-system), seja
adicionando uma tela nova, editando uma existente, extraindo um
componente, ou revisando um diff.

## As duas regras rígidas

1. **Nenhuma tela importa `androidx.compose.material3` diretamente. Nem
   mesmo `Text`.** Todo elemento visual — texto, ícones, botões, diálogos,
   app bars, scaffolds, snackbars, dividers, dropdowns, indicadores de
   progresso, campos de texto, switches, radio buttons, surfaces — passa
   por um composable wrapper do `:designsystem` (`BolaoText`, `BolaoIcon`,
   `BolaoButton`, `BolaoScaffold`, etc). Se uma tela precisa de um
   primitivo do Material 3 que ainda não tem wrapper, o certo é
   **adicionar o wrapper ao `:designsystem`**, nunca importar material3
   direto na tela.
2. **Nenhuma string literal hardcoded visível ao usuário em lugar nenhum.**
   Toda string que o usuário lê (labels, texto de botão, título/mensagem
   de diálogo, placeholders, mensagens de erro, texto de snackbar,
   content descriptions) vem de `stringResource(Res.string.xxx)`, com
   backing em `composeApp/src/commonMain/composeResources/values/strings.xml`.

As duas regras valem retroativamente — quando você mexe numa tela por um
motivo não relacionado e percebe que ela ainda importa material3 direto ou
tem strings literais, isso entra no escopo da mudança atual pra corrigir,
não é uma tarefa separada pra depois.

## Estrutura do módulo

```
designsystem/src/commonMain/kotlin/com/lpstudio/bolaodagalera/designsystem/
  theme/
    Color.kt       — todos os Color vals nomeados + gradientes (fonte única da verdade)
    Typography.kt  — BolaoTypography (escala de tipografia do Material 3)
    Shape.kt       — BolaoShapes (escala de shape do Material 3)
    Theme.kt       — BolaoTheme(content) = MaterialTheme(colorScheme, typography, shapes)
  components/
    Text.kt, Icon.kt, Button.kt, TextField.kt, Card.kt, Chip.kt,
    Dialog.kt, AppBar.kt, Scaffold.kt, Snackbar.kt, Surface.kt,
    RadioButton.kt, Switch.kt, Divider.kt, DropdownMenu.kt,
    LoadingIndicator.kt, EmptyState.kt, Avatar.kt
    — um arquivo por família de componente, cada composable prefixado com `Bolao`
```

O próprio `presentation/theme/AppTheme.kt` do `composeApp` é um wrapper
fino: ele re-exporta as cores/gradientes do design-system (pra imports
legados não quebrarem durante a migração) e combina `BolaoTheme` com
preocupações específicas do app que não pertencem a um design system
(ex: `SystemAppearance` / comportamento da status bar).

## Como é "componente, não primitivo"

Convenção de nome: `Bolao<NomeDoMaterial3>`, ex: `Text` → `BolaoText`,
`IconButton` → `BolaoIconButton`, `AlertDialog` → `BolaoConfirmDialog`
(pro formato confirmar/cancelar) ou `BolaoDialog` (genérico, pra conteúdo
customizado que não se encaixa em confirmar/cancelar). As assinaturas dos
wrappers espelham os parâmetros do composable Material 3 correspondente o
mais próximo possível — o ponto é encapsular o *import*, não restringir a
superfície da API.

Primitivos estruturais/de layout de `androidx.compose.foundation.layout`
(`Box`, `Column`, `Row`, `Spacer`, `Modifier.padding`, etc.) **não** fazem
parte dessa regra — não carregam theming do Material 3 e não precisam de
wrapper. Só imports de `androidx.compose.material3.*` são proibidos em
telas.

`SnackbarHostState` é um state holder, não um visual temático — telas usam
`rememberBolaoSnackbarHostState()` (retorna um
`typealias BolaoSnackbarHostState = SnackbarHostState`) em vez de
`remember { SnackbarHostState() }`, pra que o import de
`androidx.compose.material3` nunca apareça na tela.

Anotações `@OptIn(ExperimentalMaterial3Api::class)` devem desaparecer das
telas assim que elas pararem de chamar composables do material3
diretamente — requisitos de opt-in não se propagam através de um wrapper
estável que não é ele mesmo marcado como experimental. Se uma tela ainda
precisa da anotação depois da migração, isso é sinal de que algo não foi
totalmente encapsulado.

Quando um padrão visual genuinamente novo aparece em 2+ lugares (não só
uma ocorrência isolada), isso é sinal pra extrair como um novo componente
do `:designsystem` em vez de duplicar — isso já aconteceu com
`BolaoGlassCard` (encontrado idêntico nas telas de Login, Register e
Join) e `BolaoCard` (o padrão de 16dp/`NavyCard` repetido em várias
telas). Não force um componente numa instância visualmente diferente só
pra reduzir a contagem de arquivos — ex: um card com raio de canto ou cor
diferente é uma variante legítima, não automaticamente "o mesmo"
componente; verifique paridade exata de parâmetros antes de decidir entre
reusar, estender ou deixar como está.

## String resources

Adicione entradas em
`composeApp/src/commonMain/composeResources/values/strings.xml` (formato
XML de string-resource do Android). O plugin de resources do Compose
Multiplatform gera os acessores `Res.string.xxx` automaticamente — nunca
edite código gerado manualmente.

- Convenção de chave: `<tela>_<descrição>`, ex: `login_title`,
  `login_field_email_label`, `profile_sign_out_dialog_title`.
- Strings interpoladas usam argumentos de formato posicionais:
  `<string name="key">Remover %1$s deste bolão?</string>` chamada como
  `stringResource(Res.string.key, user.name)`.
- **Extrair sim**: qualquer coisa renderizada na tela, incluindo emoji
  usado como conteúdo de UI autônomo (ex: "🔑", "🎉") e content
  descriptions.
- **Não extrair**: strings internas que o usuário nunca vê — nomes/chaves
  de evento de analytics (`analyticsTracker.logEvent("bolao_created",
  ...)`), mensagens de contexto do crash-reporter
  (`crashReporter.recordException(e, "Erro ao...")`), mensagens de logger
  (`logger.d { "..." }`), nomes de campo do Firestore, identificadores
  internos tipo-enum. Isso é encanamento interno, não texto de UI — deixe
  como literal Kotlin comum.

## Fluxo ao migrar ou construir uma tela

1. Leia o(s) arquivo(s) alvo por completo antes de editar — anote todo
   import `androidx.compose.material3.*` e toda string literal.
2. Troque cada import material3 pelo equivalente do `:designsystem` na
   tabela de componentes existentes (confira
   `designsystem/.../components/` primeiro — a maioria dos primitivos já
   tem wrapper). Extraia cada string visível ao usuário pra `strings.xml`
   e o call site pra `stringResource(...)`.
3. Se um wrapper necessário ainda não existe, adicione-o ao
   `:designsystem` primeiro (com um `@Preview`, seguindo o estilo dos
   componentes existentes), depois use-o — não improvise uma solução
   local na tela.
4. Valide antes de considerar a mudança pronta:
   ```
   ./gradlew :designsystem:ktlintFormat :composeApp:ktlintFormat -q
   ./gradlew :designsystem:compileDebugKotlinAndroid :designsystem:compileKotlinIosSimulatorArm64 \
             :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64 \
             :composeApp:testDebugUnitTest \
             :designsystem:ktlintCheck :composeApp:ktlintCheck \
             :designsystem:detekt :composeApp:detekt -q
   ```
   Depois confirme que zero imports de material3 restam nos arquivos
   tocados: `grep -n "androidx.compose.material3" <arquivos>` deve
   retornar vazio.
5. Fique atento a regressões de window-inset ao trocar `TopAppBar` →
   `BolaoTopBar`: `BolaoTopBar` sempre zera seu próprio inset de topo
   (assume que o container externo da tela já aplica
   `.systemBarsPadding()`). Se o `TopAppBar` original não tinha override
   de `windowInsets` (usando o padrão), adicione `.systemBarsPadding()` ao
   `Box`/`Scaffold` externo da tela ao migrar, ou o conteúdo vai renderizar
   por baixo da status bar.

## Precedente: componentes já construídos

`BolaoText`, `BolaoIcon`/`BolaoIconButton`, `BolaoButton`/
`BolaoOutlinedButton`/`BolaoTextButton`, `BolaoTextField`, `BolaoCard`/
`BolaoGlassCard`, `BolaoChip`, `BolaoConfirmDialog`/`BolaoDialog`,
`BolaoTopBar`, `BolaoScaffold`, `BolaoSnackbar`/`BolaoSnackbarHost`/
`rememberBolaoSnackbarHostState`, `BolaoSurface`, `BolaoRadioButton`,
`BolaoSwitch`, `BolaoLoadingIndicator`/`BolaoFullScreenLoading`/
`BolaoLinearProgressIndicator`, `BolaoHorizontalDivider`/
`BolaoVerticalDivider`, `BolaoDropdownMenu`/`BolaoDropdownMenuItem`,
`BolaoEmptyState`, `UserAvatar`. Confira essa lista (e os arquivos reais
em `designsystem/.../components/`) antes de assumir que um wrapper não
existe.
