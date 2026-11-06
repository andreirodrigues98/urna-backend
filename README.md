# 🗳️ VotoVerso — O Multiverso Vai às Urnas

## Sobre o projeto

O **VotoVerso** é um simulador educativo de urna eletrônica desenvolvido na disciplina de **Programação Orientada a Objetos (POO)**.

A aplicação utiliza **Java 21**, interface gráfica **Java Swing**, backend **Spring Boot** e banco de dados **Firebase Cloud Firestore**.

O objetivo é demonstrar, de forma prática e interativa, o funcionamento de uma votação eletrônica, usando personagens dos universos Marvel e DC.

A votação possui duas etapas: **Deputado Federal** e **Presidente**.

| Partido | Deputado Federal | Presidente | Vice-presidente |
|---|---|---|---|
| Marvel (PM) | Homem-Aranha (`7101`) | Homem de Ferro (`71`) | Thor |
| DC (PDC) | Mulher-Maravilha (`7201`) | Superman (`72`) | Batman |

A urna permite registrar votos válidos, brancos e nulos, corrigir escolhas, visualizar os candidatos, emitir a **zerésima**, realizar a **apuração final** e salvar os votos no Firebase.

O sistema também permite reiniciar os testes, preservando o histórico das rodadas anteriores.

> **Aviso:** o VotoVerso é uma aplicação exclusivamente educativa, sem vínculo com eleições reais. Os personagens e suas imagens pertencem aos respectivos titulares de direitos autorais.

---

## Como instalar e executar o projeto

### 1. Requisitos

Para executar o VotoVerso em outro computador, é necessário:

- **Java JDK 21** instalado.
- **Visual Studio Code**, com a extensão **Extension Pack for Java**, ou outra IDE Java.
- **Windows com PowerShell**.
- Conexão com a internet.
- Projeto Firebase com **Cloud Firestore** configurado.
- Arquivo JSON de credenciais de uma conta de serviço Firebase.

**Não é necessário instalar o Maven manualmente.**

O projeto utiliza **Maven Wrapper (`mvnw.cmd`)**. As bibliotecas necessárias são baixadas automaticamente conforme as dependências do arquivo `pom.xml`.

Em Java, o `pom.xml` cumpre uma função semelhante à do `requirements.txt` utilizado em projetos Python.

### 2. Baixar e abrir o projeto

No repositório do GitHub, clique em **Code → Download ZIP**.

Extraia os arquivos e abra a pasta `urna-backend` no VS Code. Essa pasta deve conter o `pom.xml` e o `mvnw.cmd`.

Abra um terminal PowerShell dentro dessa pasta.

### 3. Configurar o Firebase

Acesse o [Firebase Console](https://console.firebase.google.com/).

Em **Configurações do projeto → Contas de serviço**, gere uma chave privada para o Firebase Admin SDK.

Salve o arquivo JSON fora do repositório, em uma pasta privada.

Exemplo:

`C:\chaves\votoverso-firebase.json`

Se a equipe já possui credenciais de teste, utilize-as somente com autorização do responsável.

No terminal PowerShell, configure as variáveis de ambiente:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\chaves\votoverso-firebase.json"

$env:FIREBASE_PROJECT_ID="ID_DO_SEU_PROJETO_FIREBASE"

$env:URNA_ELEICAO_ID="votoverso-avaliacao-01"

$env:URNA_ADMIN_KEY = Read-Host "Digite a senha administrativa"
```

Substitua o caminho do JSON e o ID do Firebase pelos valores correspondentes ao seu ambiente.

Para verificar se o arquivo de credenciais foi encontrado:

```powershell
Test-Path $env:GOOGLE_APPLICATION_CREDENTIALS -PathType Leaf
```

O resultado deve ser `True`.

O `URNA_ELEICAO_ID` identifica a eleição. Utilize o mesmo ID para acessar os votos já existentes ou escolha outro ID para uma eleição independente de teste.

**Importante:** não publique a chave JSON, a senha administrativa ou arquivos de configuração contendo segredos no GitHub.

As variáveis `$env:` ficam disponíveis somente naquele terminal. Caso ele seja fechado, será necessário configurá-las novamente, mas não é preciso recriar o Firebase.

### 4. Instalar as dependências e executar os testes

Na pasta do projeto, execute:

```powershell
java -version

.\mvnw.cmd clean test
```

O Maven fará o download automático das dependências declaradas no `pom.xml` e executará os testes automatizados.

O resultado esperado é:

```text
BUILD SUCCESS
```

### 5. Iniciar o backend Spring Boot

No mesmo terminal em que as variáveis do Firebase foram configuradas, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde o Spring Boot iniciar na porta **8080**.

Para verificar se a API está funcionando, acesse:

- [Estado da eleição](http://localhost:8080/api/eleicao/estado)
- [Lista de candidatos](http://localhost:8080/api/votacao/candidatos)

**Mantenha o terminal do Spring Boot aberto enquanto utilizar a urna.**

### 6. Iniciar a interface Java Swing

Com o backend funcionando, abra no VS Code:

`src/main/java/br/com/urnacidada/urna_swing/Main.java`

Clique em **Run Java** para iniciar a interface gráfica.

A janela do **VotoVerso** será aberta.

O Swing se comunica com o backend pelo endereço `http://localhost:8080`. As credenciais Firebase são utilizadas apenas pelo Spring Boot.

---

## Como testar a votação

Para testar uma eleição nova, siga esta sequência:

1. Inicie o backend Spring Boot e abra a interface Swing.
2. Emita a **zerésima** utilizando a senha administrativa.
3. Confira se todos os candidatos estão com zero votos.
4. Clique em **INICIAR SIMULAÇÃO**.
5. Para deputado federal, digite `7101` (Homem-Aranha) e pressione **CONFIRMA**.
6. Para presidente, digite `71` (Homem de Ferro, com Thor como vice) e pressione **CONFIRMA**.
7. Aguarde as telas **GRAVANDO** e **FIM**.
8. Consulte o estado da eleição pela API ou pelo Firebase.

Uma simulação completa registra **dois votos**, um para deputado federal e outro para presidente.

Também é possível testar os candidatos da DC com os números `7201` e `72`.

### Outros testes

Verifique também:

- **BRANCO:** registrar um voto em branco.
- **CORRIGE:** apagar uma escolha antes da confirmação.
- **NULO:** digitar um número sem candidato cadastrado e confirmar.
- **NOVA SIMULAÇÃO:** iniciar outra sessão.
- **APURAÇÃO:** encerrar a eleição e conferir o resultado.
- **EMPATE:** verificar a identificação de candidatos com a mesma votação.
- **ZERAR TESTES:** iniciar uma nova rodada mantendo o histórico anterior.

### Testar a persistência no Firebase

Após realizar uma votação:

1. Acesse o **Firebase Console → Cloud Firestore → Dados**.
2. Confira os contadores da eleição.
3. Encerre o Spring Boot com `Ctrl + C`.
4. Inicie novamente o servidor, mantendo o mesmo `URNA_ELEICAO_ID`.
5. Consulte `http://localhost:8080/api/eleicao/estado`.

Os votos registrados anteriormente deverão continuar armazenados no Firestore.

---

## Testes realizados durante o desenvolvimento

Foram realizados testes manuais da votação sequencial, reconhecimento dos candidatos, votos válidos, brancos e nulos, zerésima, persistência no Firebase, apuração, empates, reinício de rodadas e preservação do histórico.

Também foram executados testes automatizados com **JUnit 5 e Maven**.

Comando utilizado:

```powershell
.\mvnw.cmd clean test
```

Resultado da última execução registrada:

```text
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

**Resultado:** os 19 testes automatizados passaram, e os testes manuais confirmaram o funcionamento das principais funcionalidades da aplicação.

---

**Observação:** o VotoVerso utiliza Java Swing e funciona como uma aplicação desktop. O GitHub disponibiliza o código para download, mas não executa a urna como um site no navegador.