package com.luizcorrea.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	// criar animaçoes ----------------------------
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Random numeroRabdomico;
	private BitmapFont texto;
	private BitmapFont mensagem;

	private Circle passaroCirculo; // area do passado
	private Rectangle retanguloCanoTopo; // area do cano
	private Rectangle retanguloCanoBaixo;// area do cano
//	private ShapeRenderer shape; // desenhar na tela

	// ATRIBUTOS DE CONFIGURAÇÃO ------------------
	private int estadoJogo = 0; // 0->Jogo não iniciado | 1->Jogo Iniciado | 2->Jogo Game Over
	private int pontuacao =0;


	private float larguraDispositivo;
	private float alturaDispositivo;

	private float deltaTime;

	private float variacao = 0; // imagens do passaro
	private float velocidadeQueda = 0; // gravidade
	private float posicaoInicialVertical; // posição do passaro

	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float alturaEntreCanosRandomica;
	private boolean marcouPonto = false;

	// CAMERA 2D
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768; // tamanho  largura virtual
    private final float VIRTUAL_HEIGHT = 1024; // tamanho altura virtual



	@Override
	public void create () {

        /*** CONFIGUDANDO CAMERA 2D ***/
        camera = new OrthographicCamera();

        // posicionando a camera
		camera.position.set(
				VIRTUAL_WIDTH / 2,
				VIRTUAL_HEIGHT / 2,
				0
		);
        viewport = new StretchViewport(
                VIRTUAL_WIDTH , VIRTUAL_HEIGHT, camera
        );
		/*** fim ***/

        // classe para manupular a textura e imagens do jogo
		batch = new SpriteBatch();
		numeroRabdomico = new Random();
		passaroCirculo = new Circle();

/*
		// criando imagens da area de colisão
		retanguloCanoTopo = new Rectangle();
		retanguloCanoBaixo = new Rectangle();
		shape = new ShapeRenderer();
*/

		// CONFIGURAÇÃO DO TEXTO
		texto = new BitmapFont();
		texto.setColor(Color.WHITE);
		texto.getData().setScale(5);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);


		// Carregando o tamanho da tela do dispositivo
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;

		posicaoInicialVertical = alturaDispositivo/2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 400;


		// criação de textura
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo.png");
		canoTopo = new Texture("cano_topo.png");

		gameOver = new Texture("game_over.png");



	}

	@Override
	public void render () {
		camera.update();

		// limpar frame anteriores (menos memória)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


		// variacao é para alternar a animação do pássaro
		// getDeltaTime calcula a variação de taxa de atualização do render
		deltaTime = Gdx.graphics.getDeltaTime();
		variacao += deltaTime * 9;
		// limtando a entre 0 e 2 alternar as imagens do passaro
		if (variacao > 2) variacao = 0;


		if(estadoJogo == 0) { // Jogo Não Iniciado
			if(Gdx.input.justTouched()){ // verifica se tocou na tela
				estadoJogo = 1; // inicia o jogo
			}
			// fim do esta de jogo 0
		} else {
			// JOGO INICIADO ↓

			// gravidade -----------------------------------------
			velocidadeQueda++;
			if (posicaoInicialVertical > 9 || velocidadeQueda < 0)
				posicaoInicialVertical -= velocidadeQueda;


			if( estadoJogo == 1 ) {

				// movimento dos canos
				posicaoMovimentoCanoHorizontal -= deltaTime * 600; // velicidade dos canos

				// capturando touche e incrementadno subida do passaro
				if (Gdx.input.justTouched()) {
					//Gdx.app.log("touch", "tocou na tela");

					// bloqueando o passaro para não subir a mais que o tamanho da tela
					if (posicaoInicialVertical + (passaros[0].getHeight() * 2) >= alturaDispositivo) {
						velocidadeQueda = 0;
					} else {
						velocidadeQueda = -20;
					}
				}

				// movimento dos canos
				if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					alturaEntreCanosRandomica = numeroRabdomico.nextInt((int) (alturaDispositivo /2)) - (int) (alturaDispositivo /4); // gerando N + e -
					marcouPonto = false;

				}

				// VERIFICA PONTUACAO
				// quando posição do cano for menor que a posição dop passaro
				if(posicaoMovimentoCanoHorizontal < 200){
					if(!marcouPonto) {
						pontuacao++;
						Gdx.app.log("pontuação", String.valueOf(pontuacao));
						marcouPonto = true;
					}
				}
			// fim do esta de jogo 1

			} else { // tela de game over -------------------------↓ esta de jogo 2

				// reiniciando o jogo
				if(Gdx.input.justTouched()) { // se a tela for tocada
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDispositivo/2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
				}

			}

		}
		/**** Configurado dados de proteção da camera ***/
        batch.setProjectionMatrix(camera.combined); // recuperando dados de projeção



		// iniciando a exibição das imagens
		batch.begin();

		// Renderizando as imagens
		// 1º param textura, 2º=x e 3º=y param posição na tela
		// 4º=width e 5º=height param tamanho da imagem
		// metodo  Gdx.graphics.getWidth() pega a largura da tela
		// metodo  Gdx.graphics.getWiHeight() pega a altura tela

		// FUNDO
		batch.draw(fundo,0,0, larguraDispositivo, alturaDispositivo);

		// CANO TOPO
		batch.draw(canoTopo,
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo/2 + espacoEntreCanos/2 + alturaEntreCanosRandomica);

		// CANO BAIXO
		batch.draw(canoBaixo,
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight()
						- espacoEntreCanos/2 + alturaEntreCanosRandomica);

		// PASSARO
		batch.draw(passaros[(int) variacao],  200, posicaoInicialVertical);

		// TEXTO DE PONTUAÇÃO
		texto.draw(batch, String.valueOf(pontuacao),
				larguraDispositivo / 2,
				alturaDispositivo - 100);

		if( estadoJogo == 2 ) {
			batch.draw(gameOver,
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2
			);

			mensagem.draw(batch,
					"Toque para reiniciar",
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2 - gameOver.getHeight()/2
					);
		}

		// finalizar a exibição das imagens
		batch.end();



		// criando as formas  ----------------------------------------

		passaroCirculo.set(
				200 + passaros[0].getWidth()/2,
				posicaoInicialVertical + passaros[0].getHeight()/2,
				passaros[0].getHeight()/2
		);
		retanguloCanoTopo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo/2 	+ espacoEntreCanos/2 + alturaEntreCanosRandomica,
				canoTopo.getWidth(),
				canoTopo.getHeight()
		);
		retanguloCanoBaixo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight()	- espacoEntreCanos/2 + alturaEntreCanosRandomica,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()
		);

/*
		// DESENHANDO FORMAS DOS OBJETOS (passado, canos) para visualizar area de colisão(teste)
		shape.begin(ShapeRenderer.ShapeType.Filled); // forma preenchida
			shape.circle(passaroCirculo.x,passaroCirculo.y,passaroCirculo.radius); // passaro
			shape.rect(retanguloCanoBaixo.x,retanguloCanoBaixo.y,retanguloCanoBaixo.width,retanguloCanoBaixo.height); //cano baixo
			shape.rect(retanguloCanoTopo.x,retanguloCanoTopo.y,retanguloCanoTopo.width,retanguloCanoTopo.height); //cano baixo
			shape.setColor(Color.RED);
		shape.end();
*/

		// TESTE DE COLISÃO
		if(Intersector.overlaps(passaroCirculo,retanguloCanoBaixo) ||
				Intersector.overlaps(passaroCirculo,retanguloCanoTopo)
		) {
			Gdx.app.log("colisao", "bateu!");
			estadoJogo = 2;
		}

	}

    // este metodo é chamado sempre que a largura do dispositivo é alterado e quando inicia
    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
    }
}
