package br.com.guilhermecosta.estudojava.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.stereotype.Service;

import br.com.guilhermecosta.estudojava.exception.LimiteRequisicoesExcedidoException;

@Service
public class LimiteRequisicoesService {
    private static final String USUARIO_LIMITADO = "limitado";
    private static final int MAXIMO_REQUISICOES = 3;
    private static final Duration JANELA_DE_TEMPO = Duration.ofMinutes(1);
    private final Deque<Instant> requisicoesDoUsuarioLimitado = new ArrayDeque<>();

    public synchronized void validarCriacao(String nomeUsuario) {
        if (!USUARIO_LIMITADO.equals(nomeUsuario)) {
            return;
        }

        Instant agora = Instant.now();
        while (!requisicoesDoUsuarioLimitado.isEmpty()
                && requisicoesDoUsuarioLimitado.peekFirst().plus(JANELA_DE_TEMPO).isBefore(agora)) {
            requisicoesDoUsuarioLimitado.removeFirst();
        }

        if (requisicoesDoUsuarioLimitado.size() >= MAXIMO_REQUISICOES) {
            throw new LimiteRequisicoesExcedidoException();
        }

        requisicoesDoUsuarioLimitado.addLast(agora);
    }
}
