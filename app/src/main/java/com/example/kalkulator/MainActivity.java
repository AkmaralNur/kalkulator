package com.example.kalkulator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private TextView tvExpression, tvResult;
    private StringBuilder expression = new StringBuilder();
    private boolean isResultDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        setButtonListeners();
    }

    private void setButtonListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
                R.id.btnOpenBrackets, R.id.btnCloseBrackets, R.id.btnC, R.id.btnAC, R.id.btnEquals
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleButtonClick((Button) v);
                }
            });
        }
    }

    private void handleButtonClick(Button button) {
        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "C":
                clearLastCharacter();
                break;
            case "AC":
                clearAll();
                break;
            case "=":
                calculateResult();
                break;
            default:
                appendToExpression(buttonText);
                break;
        }
    }

    private void appendToExpression(String text) {
        if (isResultDisplayed) {
            expression.setLength(0);
            isResultDisplayed = false;
        }

        String operators = "+-*/";

        // 1. Удаление ведущих нулей (исключение: "0" перед точкой или другим числом)
        if (text.matches("[0-9]")) {
            if (expression.length() == 0 && text.equals("0")) {
                return;
            }
            // Запрет на ввод "03", "04" и т.д.
            if (expression.length() > 0 && expression.charAt(expression.length() - 1) == '0') {
                if (expression.length() == 1 || operators.contains(String.valueOf(expression.charAt(expression.length() - 2)))) {
                    if (!text.equals(".")) {
                        expression.setLength(expression.length() - 1);  // Удаляем лишний ноль
                    }
                }
            }
        }

        // 2. Проверка на несколько операторов подряд
        if (operators.contains(text)) {
            // Если последний символ в выражении оператор, не добавляем новый оператор
            if (expression.length() > 0 && operators.contains(String.valueOf(expression.charAt(expression.length() - 1)))) {
                return;
            }
        }

        // 3. Проверка баланса скобок
        if (text.equals(")")) {
            // Не добавляем закрывающую скобку, если нет соответствующей открывающей
            int openBrackets = 0;
            int closeBrackets = 0;
            for (int i = 0; i < expression.length(); i++) {
                if (expression.charAt(i) == '(') openBrackets++;
                if (expression.charAt(i) == ')') closeBrackets++;
            }
            if (closeBrackets >= openBrackets) {
                return;
            }
        }

        // Проверка на добавление открывающей скобки
        if (text.equals("(")) {
            // Открывающая скобка может стоять в начале или после оператора
            if (expression.length() > 0 && !operators.contains(String.valueOf(expression.charAt(expression.length() - 1)))) {
                return;
            }
        }

        // 4. Проверка на пустую скобку или неверный порядок закрытия скобок
        if (text.equals(")")) {
            if (expression.length() == 0 || operators.contains(String.valueOf(expression.charAt(expression.length() - 1)))) {
                return;
            }
        }

        // Добавляем символ в выражение
        expression.append(text);
        tvExpression.setText(expression.toString());
        tvResult.setText("");
    }

    private void clearLastCharacter() {
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            tvExpression.setText(expression.toString());
        }
    }

    private void clearAll() {
        expression.setLength(0);
        tvExpression.setText("");
        tvResult.setText("");
        isResultDisplayed = false;
    }

    private void calculateResult() {
        try {
            double result = evaluate(expression.toString());
            tvResult.setText(String.valueOf(result));
            isResultDisplayed = true;
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    // Метод для вычисления математического выражения
    private double evaluate(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operations = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            // Если символ - цифра, считываем полное число
            if (Character.isDigit(ch)) {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                i--;  // уменьшаем i, так как цикл for увеличит его снова
                numbers.push(Double.parseDouble(sb.toString()));
            }
            // Если символ - это открывающая скобка
            else if (ch == '(') {
                operations.push(ch);
            }
            // Если символ - это закрывающая скобка
            else if (ch == ')') {
                while (operations.peek() != '(') {
                    numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
                }
                operations.pop();
            }
            // Если символ - оператор
            else if (isOperator(ch)) {
                while (!operations.isEmpty() && precedence(ch) <= precedence(operations.peek())) {
                    numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
                }
                operations.push(ch);
            }
        }

        // Применяем оставшиеся операции
        while (!operations.isEmpty()) {
            numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop();
    }

    // Метод для проверки, является ли символ оператором
    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    // Метод для определения приоритета операции
    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    // Метод для применения операции к двум числам
    private double applyOperation(char operation, double b, double a) {
        switch (operation) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                return a / b;
            default:
                return 0;
        }
    }
}
