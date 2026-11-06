# VotoVerso — O Multiverso Vai às Urnas 🗳️

**Simulador educativo de votação eletrônica desenvolvido em Java 21, com interface Swing, API REST em Spring Boot e persistência de dados no Firebase Cloud Firestore.**

O VotoVerso é um projeto acadêmico da disciplina de Programação Orientada a Objetos (POO). Seu objetivo é demonstrar, de forma interativa, o funcionamento básico de uma urna eletrônica, permitindo que estudantes realizem uma eleição simulada com personagens fictícios dos universos Marvel e DC.

O projeto possui duas etapas de votação: **Deputado Federal** e **Presidente**.

> **Aviso:** esta aplicação é exclusivamente educativa. Não possui vínculo com eleições reais e não deve ser utilizada para processos eleitorais oficiais.

## Funcionalidades

- Interface gráfica desktop desenvolvida com Java Swing.
- Teclado numérico e botões BRANCO, CORRIGE e CONFIRMA.
- Votação sequencial para deputado federal e presidente.
- Exibição do nome, partido, número e fotografia dos candidatos.
- Exibição do vice-presidente da chapa.
- Prévia rápida dos candidatos.
- Animação de gravação do voto e tela FIM.
- Zerésima para registrar a abertura da eleição com zero votos.
- Persistência dos votos e das sessões no Firebase Cloud Firestore.
- Votos válidos, brancos e nulos.
- Apuração final com contagem e identificação dos vencedores.
- Exibição das fotos dos vencedores na apuração.
- Apuração com apenas texto quando houver empate.
- Bloqueio de novas votações após o encerramento.
- Função ZERAR TESTES, com autenticação administrativa e histórico das rodadas.
- Testes automatizados com JUnit.

## Partidos e candidatos

| Partido | Deputado Federal | Presidente | Vice-presidente |
|---|---|---|---|
| Partido Marvel (PM) | Homem-Aranha — 7101 | Homem de Ferro — 71 | Thor |
| Partido DC (PDC) | Mulher-Maravilha — 7201 | Superman — 72 | Batman |

Os personagens são utilizados apenas na simulação educativa. As imagens e os personagens pertencem aos respectivos titulares de direitos autorais.

## Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem principal |
| Java Swing / Java2D | Interface gráfica |
| Spring Boot 4.1.2 | API REST e regras de negócio |
| Firebase Cloud Firestore | Persistência dos dados |
| Firebase Admin SDK | Comunicação segura do backend com o Firebase |
| Maven | Gerenciamento das dependências |
| JUnit 5 | Testes automatizados |
| Git e GitHub | Versionamento e publicação do código |

## Arquitetura

O projeto é dividido em interface gráfica, backend e banco de dados.

```text
Java Swing
    |
    | HTTP / API REST
    v
Spring Boot
    |
    | Firebase Admin SDK
    v
Cloud Firestore
```

A interface Swing não acessa diretamente as credenciais do Firebase. Toda comunicação com o banco é realizada pelo Spring Boot.

### Organização dos arquivos

```text
urna-backend/
├── pom.xml
├── mvnw.cmd
├── README.md
│
├── scripts/
│   ├── iniciar-backend.ps1
│   └── iniciar-interface.ps1
│
├── testes/
│   └── testar-api.ps1
│
└── src/
    ├── main/
    │   ├── java/br/com/urnacidada/
    │   │   ├── urna_backend/
    │   │   │   ├── config/
    │   │   │   ├── controller/
    │   │   │   ├── model/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   └── urna_swing/
    │   │       ├── Main.java
    │   │       ├── UrnaFrame.java
    │   │       └── UrnaApiClient.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       └── imagens/
    │
    └── test/java/
```

## Requisitos para execução

Antes de executar a aplicação, é necessário ter:

- Java JDK 21 instalado.
- Windows com PowerShell.
- VS Code com suporte a Java, ou outro ambiente de desenvolvimento Java.
- Acesso à internet.
- Projeto Firebase com Cloud Firestore configurado.
- Arquivo JSON de credenciais de uma conta de serviço Firebase.

O projeto utiliza Maven Wrapper, portanto não é necessário instalar o Maven separadamente para executar os comandos `mvnw.cmd`.

## Instalação e execução

### 1. Abrir o projeto

Baixe ou clone o repositório e abra a pasta `urna-backend` no VS Code.

No PowerShell, entre na pasta:

```powershell
cd C:\andrei_dev\urna-backend
```

Se você extraiu o projeto em outro local, altere o caminho.


### 2. Configurar as variáveis de ambiente

No terminal PowerShell que será utilizado para iniciar o backend:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\Users\Andrei Rodrigues\Downloads\chaves/votoverso-firebase.json"

$env:FIREBASE_PROJECT_ID="urna-backend-db"

$env:URNA_ELEICAO_ID="votoverso-teste-01"

$env:URNA_ADMIN_KEY = Read-Host "Digite a senha administrativa"
```

**Importante:** ajuste o caminho do JSON para corresponder ao arquivo existente no seu computador.

As variáveis possuem as seguintes funções:

| Variável | Descrição |
|---|---|
| `GOOGLE_APPLICATION_CREDENTIALS` | Caminho completo para o JSON privado |
| `FIREBASE_PROJECT_ID` | Identificador do projeto Firebase |
| `URNA_ELEICAO_ID` | Identificador da eleição simulada |
| `URNA_ADMIN_KEY` | Senha administrativa definida pela equipe |

Para verificar se a credencial existe:

```powershell
Test-Path $env:GOOGLE_APPLICATION_CREDENTIALS -PathType Leaf
```

O resultado esperado é `True`.

**Atenção:** mantenha o mesmo `URNA_ELEICAO_ID` para continuar acessando os dados de uma eleição existente. Uma eleição independente deve usar outro identificador.

### 4. Iniciar o backend Spring Boot

No mesmo terminal onde as variáveis foram configuradas, execute:

```powershell
.\scripts\iniciar-backend.ps1
```

Alternativamente:

```powershell
.\mvnw.cmd spring-boot:run
```

O Spring Boot será iniciado na porta 8080.

Para verificar:

- [Status da aplicação](http://localhost:8080/status)
- [Estado da eleição](http://localhost:8080/api/eleicao/estado)
- [Lista de candidatos](http://localhost:8080/api/votacao/candidatos)

**Mantenha o terminal do backend aberto durante a utilização da urna.**

### 5. Executar a interface Swing

Abra outro terminal PowerShell na mesma pasta e execute:

```powershell
.\scripts\iniciar-interface.ps1
```

A interface do VotoVerso será aberta.

Também é possível executar a classe `Main.java` diretamente pelo VS Code utilizando **Run Java**.

O segundo terminal não precisa das credenciais Firebase, pois somente o backend acessa o Firestore.

## Como utilizar a urna

1. Inicie o Spring Boot e a interface Swing.
2. Em uma eleição nova, emita a zerésima com a senha administrativa.
3. Confira se todos os candidatos possuem zero votos.
4. Clique em INICIAR SIMULAÇÃO.
5. Escolha o deputado federal digitando quatro números.
6. Confira a foto, o nome e o partido.
7. Pressione CONFIRMA.
8. Escolha o presidente digitando dois números.
9. Confira o presidente e o vice-presidente.
10. Pressione CONFIRMA.
11. Aguarde GRAVANDO e depois FIM.
12. Utilize NOVA SIMULAÇÃO para permitir outra votação.

Cada simulação completa registra dois votos: um para deputado federal e outro para presidente.

### Encerramento e apuração

Após a conclusão das votações, o responsável pela atividade utiliza a opção de encerramento e informa a senha administrativa.

A apuração apresenta:

- Quantidade de votos de cada candidato.
- Votos em branco.
- Votos nulos.
- Resultado de deputado federal.
- Resultado da chapa presidencial.
- Fotos dos vencedores, quando houver vencedor único.
- Mensagem de empate, quando aplicável.

Depois do encerramento, novas votações são bloqueadas até a abertura de outra rodada.

### Zerésima e reinício dos testes

A zerésima registra os contadores zerados antes da primeira votação da rodada.

O botão **ZERAR TESTES** fica disponível somente quando existem votos registrados. Ele exige confirmação administrativa e permite iniciar outra rodada sem misturar os contadores.

Os resultados anteriores ficam arquivados na subcoleção `rodadas` do Firestore.


2. Configurar o Firebase

Acesse o Firebase Console e selecione seu projeto.

Em Configurações do projeto → Contas de serviço, gere uma chave privada para o Firebase Admin SDK.

Guarde o arquivo JSON em uma pasta privada, fora do repositório, por exemplo:

C:\chaves\votoverso-firebase.json