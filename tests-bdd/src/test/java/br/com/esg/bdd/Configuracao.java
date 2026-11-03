package br.com.esg.bdd;

/** Configuracao lida de propriedades -D ou de variaveis de ambiente, com padroes para rodar localmente. */
public final class Configuracao {

    public static final String BASE_URL = valor("base.url", "BASE_URL", "http://localhost:8080");
    public static final String ADMIN_EMAIL = valor("admin.email", "ADMIN_EMAIL", "admin@esg.com");
    public static final String ADMIN_SENHA = valor("admin.senha", "ADMIN_SENHA", "admin123");
    public static final String OPERADOR_EMAIL = valor("operador.email", "OPERADOR_EMAIL", "operador@esg.com");
    public static final String OPERADOR_SENHA = valor("operador.senha", "OPERADOR_SENHA", "operador123");

    private Configuracao() {
    }

    private static String valor(String propriedade, String variavel, String padrao) {
        String valor = System.getProperty(propriedade);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variavel);
        }
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
