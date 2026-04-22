# Bibliosys — sistema de gestão de biblioteca

Sistema web completo para gestão de biblioteca, desenvolvido com Spring Boot e MongoDB Atlas. Permite que bibliotecários gerenciem o acervo, empréstimos e reservas, enquanto leitores acompanham seus empréstimos, reservas e interagem com o suporte diretamente pela plataforma.

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.5.1 | Framework web |
| Spring Data MongoDB | — | Persistência de dados |
| Spring Security | — | Autenticação e autorização |
| Thymeleaf | — | Template engine (SSR) |
| Lombok | — | Redução de boilerplate |
| MongoDB Atlas | Cloud | Banco de dados NoSQL |
| Chart.js | 4.4.1 | Gráficos nos dashboards |

---

## Funcionalidades

### Bibliotecário (Admin)

- **Gestão de Livros** — cadastrar, editar, excluir e buscar livros no acervo
- **Gestão de Usuários** — cadastrar e gerenciar leitores e bibliotecários
- **Empréstimos** — registrar empréstimos com prazo de 14 dias e controle de multas (R$ 1,00/dia)
- **Devoluções** — registrar devoluções com atualização automática do estoque e fila de reservas
- **Reservas** — visualizar e gerenciar fila de reservas com sistema FIFO automático
- **Central de Suporte** — visualizar, responder e resolver chamados abertos pelos leitores; visualizar avaliação do atendimento
- **Relatórios** — painel com gráfico de pizza (status dos empréstimos), gráfico de barras (top 5 livros), tabela dos últimos 5 empréstimos e top 5 usuários mais ativos

### Leitor

- **Dashboard** — painel pessoal com gráfico doughnut de empréstimos, resumo de reservas, acesso rápido e empréstimos recentes
- **Catálogo** — buscar livros disponíveis e solicitar empréstimo ou reserva diretamente
- **Meus Empréstimos** — acompanhar status, prazo de devolução e multas em aberto
- **Minhas Reservas** — acompanhar posição na fila e notificação quando disponível para retirada
- **Suporte** — abrir chamados de dúvida, reclamação ou sugestão; acompanhar respostas; avaliar atendimento com estrelas (1–5) e reação (👍 😐 👎)
- **Cadastro** — auto-cadastro público, sem necessidade de intervenção do admin

### Regras de Negócio

- Usuário com empréstimo **ATRASADO** não pode realizar novos empréstimos nem reservas
- Limite de **3 empréstimos simultâneos** por usuário
- Reservas só podem ser feitas para livros **indisponíveis**
- Fila de reservas segue ordem **FIFO** (primeiro a reservar, primeiro a ser notificado)
- Reservas expiram automaticamente após **48 horas** sem confirmação de retirada (job agendado via `@Scheduled`)
- Ao realizar um empréstimo, reservas pendentes do mesmo livro/usuário são canceladas automaticamente
- Empréstimos com prazo em até 3 dias exibem alerta **"Vencendo em breve"**
- Multa por atraso: **R$ 1,00 por dia**

---

## Pré-requisitos

- **Java 17** ou superior
- **Maven** (ou usar o wrapper `./mvnw` incluído no projeto)
- Conta no **MongoDB Atlas** com um cluster criado

---

## Como Configurar e Rodar

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd biblioteca
```

### 2. Configurar o banco de dados

Abra o arquivo `src/main/resources/application.properties` e substitua a URI de conexão pela sua:

```properties
spring.data.mongodb.uri=mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/biblioteca?retryWrites=true&w=majority
```

### 3. Rodar o projeto

```bash
./mvnw spring-boot:run
```

### 4. Acessar no navegador

```
http://localhost:8080
```

---

## Credenciais Padrão

> O primeiro usuário administrador deve ser criado manualmente no MongoDB Atlas ou pelo formulário `/cadastro`, alterando o campo `role` para `BIBLIOTECARIO` diretamente na coleção `usuarios`.

| Perfil | E-mail | Senha |
|---|---|---|
| Bibliotecário | `admin@biblioteca.com` | `admin123` |
| Leitor | *(cadastro em `/cadastro`)* | *(definida no cadastro)* |

Novos leitores podem se cadastrar pelo formulário público em `/cadastro` sem intervenção do administrador.

---

## Estrutura do Projeto

```
biblioteca/
├── src/
│   └── main/
│       ├── java/com/thiago/biblioteca/
│       │   ├── config/          # SecurityConfig, LoginSuccessHandler
│       │   ├── controller/      # Controllers MVC (um por módulo)
│       │   ├── model/           # Entidades MongoDB + UsuarioDetails
│       │   ├── repository/      # Interfaces Spring Data MongoDB
│       │   └── service/         # Regras de negócio
│       └── resources/
│           ├── templates/       # Templates Thymeleaf
│           │   ├── emprestimos/ # lista.html, form.html
│           │   ├── leitor/      # dashboard.html, catalogo.html, meus-emprestimos.html, minhas-reservas.html
│           │   ├── livros/      # lista.html, form.html
│           │   ├── relatorios/  # index.html
│           │   ├── reservas/    # lista.html, form.html
│           │   ├── suporte/     # lista.html, novo.html, responder.html, ver.html, acompanhar.html
│           │   ├── usuarios/    # lista.html, form.html
│           │   ├── layout.html          # Sidebar do bibliotecário
│           │   ├── layout-leitor.html   # Sidebar do leitor
│           │   └── login.html
│           └── application.properties
├── mvnw
└── pom.xml
```

---

## Endpoints Principais

### Públicos

| Método | Rota | Descrição |
|---|---|---|
| GET | `/login` | Página de login |
| GET/POST | `/cadastro` | Cadastro de novo leitor |
| GET | `/suporte/novo` | Abrir chamado de suporte |
| POST | `/suporte/salvar` | Salvar chamado |

### Bibliotecário

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Dashboard admin |
| GET | `/livros` | Listar livros |
| GET/POST | `/livros/novo` | Cadastrar livro |
| GET/POST | `/livros/editar/{id}` | Editar livro |
| POST | `/livros/deletar/{id}` | Excluir livro |
| GET | `/emprestimos` | Listar empréstimos |
| GET/POST | `/emprestimos/novo` | Registrar empréstimo |
| POST | `/emprestimos/devolver/{id}` | Registrar devolução |
| GET | `/reservas` | Listar reservas |
| POST | `/reservas/concluir/{id}` | Concluir reserva |
| POST | `/reservas/cancelar/{id}` | Cancelar reserva |
| GET | `/usuarios` | Listar usuários |
| GET/POST | `/usuarios/novo` | Cadastrar usuário |
| GET | `/suporte` | Listar chamados |
| GET/POST | `/suporte/responder/{id}` | Responder chamado |
| GET | `/suporte/ver/{id}` | Ver chamado completo com avaliação |
| GET | `/relatorios` | Painel de relatórios com gráficos |

### Leitor

| Método | Rota | Descrição |
|---|---|---|
| GET | `/leitor/dashboard` | Dashboard do leitor com gráficos |
| GET | `/catalogo` | Catálogo de livros |
| POST | `/catalogo/solicitar` | Solicitar empréstimo |
| POST | `/catalogo/reservar` | Reservar livro indisponível |
| GET | `/meus-emprestimos` | Histórico de empréstimos |
| GET | `/minhas-reservas` | Reservas ativas |
| POST | `/minhas-reservas/cancelar/{id}` | Cancelar reserva |
| GET | `/suporte/acompanhar` | Acompanhar chamados pelo e-mail do login |
| POST | `/suporte/avaliar/{id}` | Avaliar resposta do suporte |

---

## Autor

**Thiago Henrique Yaginuma**
Curso: Engenharia de Software — UMC
Ano: 2026
