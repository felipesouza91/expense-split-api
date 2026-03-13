package dev.fsantana.expensesplitapi.infra.config.api.dto;

import lombok.Getter;

@Getter
public enum ProblemType {

  RECURSO_NAO_ENCONTRADO("Recurso não encontrado", "/recurso-nao-encontrado"),
  ENTIDADE_EM_USO("Entidade em uso", "/entidade-em-uso"),
  ERRO_NEGOCIO("Violação de regra de negocio", "/erro-negocio"),
  MENSAGEM_INCOMPREENSIVEL("Mensagem Incompreesivel", "/mensagem-incompreensivel"),
  PARAMETRO_INVALIDO("Parametro Invalido", "/parametro-invalido"),
  ERRO_DE_SISTEMA("Erro de Sistema", "/erro-de-sistema"), DADOS_INVALIDOS("Dados Invalidos",
      "/dados-invalidos"),
  ACESSO_NEGADO("Acesso Negado", "/acesso-negado"),
  REGRA_DE_NEGOCIO("Violação Regra de Negocio", "/regra-de-negocio");

  private final String title;

  private final String path;

  ProblemType(String title, String path) {
    this.title = title;
    this.path = path;
  }
}