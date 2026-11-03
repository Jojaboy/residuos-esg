# Passo a passo: rodar o pipeline e tirar os prints (sem Docker no computador)

Todo o build, testes e deploys rodam nos servidores do GitHub. Você só precisa de uma conta no GitHub e um navegador.

## 1. Criar o repositório e enviar o código

1. Em https://github.com/new crie um repositório chamado `residuos-esg` como **público** (os ambientes staging/production do GitHub ficam disponíveis no plano gratuito para repositórios públicos) e não marque "Add a README".
2. Envie o código. Escolha uma das opções:

   **Com git (terminal):**
   ```bash
   cd residuos-esg
   git init
   git add .
   git commit -m "Projeto Cidades ESG Inteligentes - DevOps"
   git branch -M main
   git remote add origin https://github.com/<seu-usuario>/residuos-esg.git
   git push -u origin main
   ```

   **Sem git:** use o GitHub Desktop (File → Add local repository → Publish), ou na página do repositório clique em *uploading an existing file* e arraste **todo o conteúdo** da pasta (confira se a pasta `.github` foi junto).

3. O push na `main` dispara o pipeline sozinho. Acompanhe na aba **Actions**. A primeira execução leva uns 8 a 12 minutos (o Oracle é baixado duas vezes, uma por ambiente).

## 2. Prints para tirar (salve com estes nomes em `docs/prints/`)

| Arquivo | Onde tirar |
|---|---|
| `01-pipeline.png` | Actions → clique na execução → tela com o desenho dos 5 jobs todos verdes |
| `02-build.png` | Clique no job **1. Build** → expanda o passo "Compilar e empacotar" mostrando `BUILD SUCCESS` |
| `03-testes.png` | Na execução, role a página **Summary** até a tabela "Testes automatizados" (21 testes, 0 falhas) |
| `04-imagem-ghcr.png` | Página do repositório → lateral direita **Packages** → `residuos-esg` (mostra as tags `latest`, `production`, `staging`, `sha-...`) |
| `05-staging.png` | Summary → seção **"Staging no ar"** (docker compose ps + `/api/info` com `"ambiente": "staging"`). Pode juntar com o passo "Smoke test do staging" do job 4 |
| `06-producao.png` | Summary → seção **"Producao no ar"** (`"ambiente": "production"`), ou o passo "Smoke test da producao" do job 5 |
| `07-deployments.png` | Página inicial do repositório → lateral direita **Deployments** → mostra *staging* e *production* com status Active |

Depois de salvar os prints na pasta `docs/prints/`, envie-os ao GitHub (commit) para aparecerem no README, e cole os mesmos prints nos espaços marcados do PDF de documentação.

## 3. (Opcional) Ver a API no navegador com Swagger, sem instalar nada

1. No repositório: botão verde **Code → Codespaces → Create codespace on main**.
2. No terminal do Codespace:
   ```bash
   cp .env.example .env
   docker compose up -d --build
   ```
3. Quando a porta 8080 aparecer na aba **Ports**, abra o endereço e acrescente `/swagger-ui.html`. Ótimo print extra para o PDF.
4. Ao terminar, pare o Codespace para não gastar a cota gratuita.

## 4. (Opcional) Aprovação manual antes da produção

Settings → Environments → **production** → marque *Required reviewers* e coloque seu usuário. Na próxima execução o job 5 vai esperar você clicar em **Approve** — é mais um print interessante de controle de deploy.

## 5. Se algo der errado

- **Job 3 falhou com "denied" ao publicar a imagem:** Settings → Actions → General → *Workflow permissions* → marque **Read and write permissions** e rode de novo (Actions → execução → *Re-run all jobs*).
- **Algum teste falhou:** abra o job 2, o erro aparece no log e nos relatórios (artefato `relatorios-testes`).
- **Deploy falhou:** baixe o artefato `logs-deploy-staging` / `logs-deploy-production` com os logs dos containers.
