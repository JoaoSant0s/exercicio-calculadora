package br.ufpe.cin.if710.calculadora

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    // @comment
    // Manipulando o resultado e a expressão inserida pelo usuário com variáveis privadas

    private var textCalculator = "";
    private var resultCalculator = "";

    // @comment
    // Criando chaves para recuperação de valores, quando o layout do app é modificado
    private val SAVED_TEXT_CALC = "savedTextCalc"
    private val SAVED_RESULT_CALC = "savedResultCalc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonsEvents();
    }

    // @comment
    // Override do método de salvamento de contexto, para salvar o resultado e a expressão
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState);
        outState?.putString(SAVED_TEXT_CALC, textCalculator);
        outState?.putString(SAVED_RESULT_CALC, resultCalculator);
    }

    // @comment
    // Override do método de recuperação de contexto, para retornar e setar o resultado e a expressão
    // A expressão será a última digitada pelo usuário, mas o resultado será o último impresso
    // [OBS] No meu aparelho, no layout landscap, não consegui acessar os botões inferiores, imagino
    // que seja porque o layout da activity não foi projetado para isso
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState);
        var savedText = savedInstanceState?.getString(SAVED_TEXT_CALC);
        var savedResult = savedInstanceState?.getString(SAVED_RESULT_CALC);

        restoreCalculatorText(savedText);
        restoreCalculatorResult(savedResult);
    }

    // @comment
    // Funcção chamada para restaurar o valor do resultado
    private fun restoreCalculatorResult(value : String?){
        resultCalculator = value.orEmpty();
        text_info.text = resultCalculator;
    }

    // @comment
    // Funcção chamada para restaurar o valor da expressão
    private fun restoreCalculatorText(value : String?){
        textCalculator = value.orEmpty();
        text_calc.setText(textCalculator);
    }

    // @comment
    // Funcção chamada para gerar o resultado da expressão e mostra-lo na tela.
    // Caso encontre uma excessão, deverá mostar o aviso em tela
    // [OBS] Na impressão do erro, meu aparelho não conseguiu renderizar qual character estavá incorreto
    // Ele mostrava uma caixa, indicando character não reconhecido, pegando apenas com parêntesis.
    private fun onGenerateResult(){
        try {
            var value =  eval(textCalculator);
            resultCalculator = value.toString();
            text_info.text = resultCalculator;
        }catch (err: RuntimeException){
            Toast.makeText(this@MainActivity, err.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // @comment
    // Funcção chamada para adicionar valores no final da expressão
    private fun onAddCalculatorValue(key : String){
        textCalculator+=key
        text_calc.setText(textCalculator);
    }

    // @comment
    // Funcção chamada para remover o último valor definido na expressão
    private fun onRemoveCalculatorValue(){
        if(textCalculator.isEmpty()) return;
        textCalculator = textCalculator.substring(0, textCalculator.length - 1);
        text_calc.setText(textCalculator);
    }

    // @comment
    // Funcção base shamada para adicionar os principais listeners da activity
    // Ela utiliza de um loop em botões que possuem a mesma characterística base, reduzindo a quantidade de código
    // que seria produzida, se fossemos colocar "setOnClickListener" em cada botão, separadamente
    private fun setButtonsEvents(){
        btn_Clear.setOnClickListener {
            onRemoveCalculatorValue();
        }
        btn_Equal.setOnClickListener {
            onGenerateResult();
        }

        val buttonList = arrayListOf<Button>(btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9,
                                                btn_Divide, btn_Multiply, btn_Add, btn_Subtract, btn_Dot, btn_LParen, btn_RParen, btn_Power);

        buttonList.forEach(){
            setButtonEvent(it);
        }
    }

    // @comment
    // Funcção chamada para definir os eventos de click
    private fun setButtonEvent(btn : Button){
        btn.setOnClickListener (){
            onAddCalculatorValue(btn.text.toString());
        }
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
