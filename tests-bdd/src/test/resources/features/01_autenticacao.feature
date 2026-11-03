# language: pt
@api @autenticacao
Funcionalidade: Autenticação na API de Gestão de Resíduos
  Como operador ou administrador do sistema ESG
  Quero entrar com meu e-mail e senha
  Para que somente pessoas autorizadas alterem os dados de coleta

  Contexto:
    Dado que a API está no ar

  @positivo
  Esquema do Cenário: Login com credenciais válidas retorna um token JWT
    Quando faço login com e-mail "<email>" e senha "<senha>"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "token.json"
    E o campo "tipo" deve ser "Bearer"
    E o campo "perfil" deve ser "<perfil>"
    E o campo "token" deve estar preenchido

    Exemplos:
      | email            | senha       | perfil   |
      | admin@esg.com    | admin123    | ADMIN    |
      | operador@esg.com | operador123 | OPERADOR |

  @negativo
  Esquema do Cenário: Login com credenciais incorretas é recusado
    Quando faço login com e-mail "<email>" e senha "<senha>"
    Então o status da resposta deve ser 401
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "E-mail ou senha invalidos"

    Exemplos:
      | email              | senha        |
      | admin@esg.com      | senha-errada |
      | ninguem@esg.com    | admin123     |

  @negativo
  Esquema do Cenário: Login com dados inválidos é recusado
    Quando faço login com e-mail "<email>" e senha "<senha>"
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"
    E os detalhes do erro devem citar o campo "<campo>"

    Exemplos:
      | email         | senha    | campo |
      | nao-e-email   | admin123 | email |
      | admin@esg.com |          | senha |

  @negativo
  Cenário: Login com JSON mal formatado é recusado
    Quando envio uma requisição POST para "/api/auth/login" com o corpo:
      """
      {"email": "admin@esg.com",
      """
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "mal formatado"
