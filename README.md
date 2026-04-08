# PW - Player Warps System

Sistema de Player Warps de alta performance com interface dinâmica (GUI), focado em simplicidade para jogadores Java e Bedrock.

## 🚀 Funcionalidades

*   **GUI Paginada:** Sistema de menus organizado com slots dinâmicos e suporte a Geyser/Folia.
*   **Integração com Proteções:** Compatível com **WorldGuard**, **GriefPrevention** e **RedProtect**. Jogadores só criam warps onde possuem Trust/Build permission.
*   **Teleporte Seguro:** Validação automática de blocos (bloqueia lava, água, ar, plantações, cercas, etc).
*   **Suporte a Banco de Dados:** Opção entre **SQLite** (local) ou **MySQL** (rede).
*   **Multi-Linguagem:** Tradução completa via arquivos (PT, EN, ES, RU).
*   **Anúncios Webhook:** Notificações opcionais no Discord para criação, deleção e teleporte.
*   **Limites por Permissão:** Configure limites de warps via permissões (ex: `pw.limit.10`).

## 🛠 Comandos e Permissões


| Comando | Descrição | Permissão |
| :--- | :--- | :--- |
| `/pw` | Abre o menu principal ou teleporta para uma warp | `pw.use` |
| `/pwset <nome>` | Cria uma nova warp no local atual | `pw.set` |
| `/pwedit <nome>` | Abre o painel de edição da sua warp | `pw.edit` |
| `/pweditplayer` | Administradores editam qualquer warp | `pw.admin` |
| `/pwsetname` | Altera o nome de uma warp existente | `pw.setname` |
| `/pwsetlore` | Altera a descrição (lore) da warp | `pw.setlore` |
| `/pwseticon` | Altera o ícone usando o item da mão | `pw.seticon` |
| `/pwdel <nome>` | Remove uma warp permanentemente | `pw.del` |
| `/pwreset <nome>`| Redefine a localização para sua posição atual | `pw.reset` |

## 📦 Instalação

1.  Coloque o arquivo `PW.jar` na sua pasta `plugins/`.
2.  Certifique-se de ter o **Vault** instalado (Dependência Obrigatória).
3.  (Opcional) Tenha WorldGuard, GriefPrevention ou RedProtect para travas de terreno.
4.  Reinicie o servidor para gerar as pastas e arquivos de configuração.
