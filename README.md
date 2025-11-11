# API StudyHub 

A API StudyHub é um *backend* robusto, construído em **Spring Boot** (Java) e **MongoDB**, projetado para gerenciar um banco de questões e simulados. Esta versão estabelece uma arquitetura completa com foco em **Segurança, Autorização e Escalabilidade de Dados**.

## 🌟 Destaques da Arquitetura

| Pilar | Implementação Técnica |
| :--- | :--- |
| **Segurança (Auth)** | **JWT** (JSON Web Tokens) para autenticação *Stateless*. |
| **Autorização (Permissões)**| **RBAC** (Role-Based Access Control): Proteção de rotas para o papel **ADMIN**. |
| **Criptografia** | **BCrypt** para hashing seguro de senhas. |
| **Busca de Dados** | **Filtros Dinâmicos** com Regex (busca parcial/case-insensitive) e **Paginação**. |
| **Arquitetura** | Separação clara em camadas: Controller → Service → Repository. |

---

## Configuração e Inicialização

### Pré-requisitos

* **Java JDK 21+**
* **Apache Maven**
* **MongoDB:** Uma instância acessível (local ou Atlas).

### Variáveis de Ambiente Obrigatórias

O projeto exige que as seguintes variáveis de ambiente sejam definidas para a segurança e conexão:

| Variável | Descrição |
| :--- | :--- |
| `MONGODB_URI` | A URI de conexão completa com o MongoDB. |
| `STUDYHUB_JWT_SECRET_KEY` | A chave secreta Base64 (forte e longa) para assinar os tokens JWT. |

### Configuração do Projeto (`application.properties`)

O arquivo de propriedades deve referenciar as variáveis de ambiente:

```properties
# Configuração do Banco de Dados
spring.data.mongodb.uri=${MONGODB_URI}

# Configuração do JWT (Chave de Segurança)
jwt.secret.key=${STUDYHUB_JWT_SECRET_KEY}
jwt.expiration.time=28800000 # 8 horas (Tempo para expirar o token)
```

## Inicialização

Execute a aplicação via terminal:

```bash
./mvnw spring-boot:run
```
A API estará disponível em http://localhost:8080.

## Endpoints da API

### I. Autenticação e Usuários (`/auth` e `/user`)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| **`POST`** | `/auth/signup` | Registro de novo usuário (`role: USER`). | Livre |
| **`POST`** | `/auth/signin` | Login. Retorna o JWT (Token Bearer). | Livre |
| **`GET`** | `/user` | Obtém dados do perfil logado (inclui role e contagem de simulados). | **Autenticado** |
| **`DELETE`** | `/user` | Permite ao usuário deletar a própria conta. | **Autenticado** |

### II. Gerenciamento de Questões (`/questoes`)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| **`POST`** | `/questoes` | Cria uma lista de questões (inserção em lote). | **ADMIN** |
| **`GET`** | `/questoes` | Busca Dinâmica (Filtros, Paginação). | Livre |
| **`GET`** | `/questoes/{id}` | Busca uma questão específica pelo ID. | Livre |
| **`DELETE`** | `/questoes/{id}` | Deleta uma questão pelo ID. | **ADMIN** |

### III. Gerenciamento de Simulados (`/simulados`)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| **`POST`** | `/simulados` | Cria um novo simulado, associando-o ao `idUser` logado. | **Autenticado** |
| **`GET`** | `/simulados` | Lista **APENAS** os simulados pertencentes ao usuário logado. | **Autenticado** |
| **`GET`** | `/simulados/{id}` | Busca um simulado específico pelo ID, **verificando se ele pertence ao usuário logado**. | **Autenticado** |
| **`DELETE`** | `/simulados/{id}` | Deleta um simulado específico, **verificando se ele pertence ao usuário logado**. | **Autenticado** |

---

## Uso: Busca Dinâmica e Paginação

O endpoint `GET /questoes` permite a combinação de filtros e controle total sobre os resultados.

### Estrutura da URL:

```
GET /questoes?disciplina=valor & dificuldade=valor & page=0 & size=20
```

| Parâmetro | Tipo | Descrição da Busca |
| :--- | :--- | :--- |
| `disciplina`, `instituicao`, `termo` | `String` | Busca Parcial/Flexível (ignora maiúsculas/minúsculas). |
| `dificuldade` | `String` | Busca exata. Valor deve ser **FACIL, MEDIO ou DIFICIL**. |
| `page` | `Integer` | Índice da página a ser retornada (padrão 0). |
| `size` | `Integer` | Número de itens por página (padrão 20). |

### Exemplo Completo de Busca:

Para buscar 10 questões de Física na Página 2 que contenham o termo "relatividade":
```
GET /questoes?disciplina=fisica&termo=relatividade&page=1&size=10
```
