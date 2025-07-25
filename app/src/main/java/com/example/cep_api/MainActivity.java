package com.example.cep_api;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCEP;
    private Button buttonConsultar;
    private TextView textViewLogradouro;
    private TextView textViewBairro;
    private TextView textViewLocalidade;
    private TextView textViewUF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar os componentes da UI
        editTextCEP = findViewById(R.id.editTextCEP);
        buttonConsultar = findViewById(R.id.buttonConsultar);
        textViewLogradouro = findViewById(R.id.textViewLogradouro);
        textViewBairro = findViewById(R.id.textViewBairro);
        textViewLocalidade = findViewById(R.id.textViewLocalidade);
        textViewUF = findViewById(R.id.textViewUF);

        // 2. Configurar o listener de clique para o botão
        buttonConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cep = editTextCEP.getText().toString().trim();

                // 3. Validar se o CEP foi digitado e tem o tamanho correto
                if (cep.isEmpty() || cep.length() != 8) {
                    Toast.makeText(MainActivity.this, "Por favor, digite um CEP válido com 8 dígitos.", Toast.LENGTH_SHORT).show();
                } else {
                    // 4. Executar a consulta em segundo plano usando AsyncTask
                    new ConsultarCEPATask().execute(cep);
                }
            }
        });
    }

    // AsyncTask para realizar a requisição de rede em segundo plano
    private class ConsultarCEPATask extends AsyncTask<String, Void, String> {

        // Este método é executado antes da operação em segundo plano
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Opcional: Mostrar um indicador de carregamento (ProgressBar)
            Toast.makeText(MainActivity.this, "Consultando CEP...", Toast.LENGTH_SHORT).show();
        }

        // Este método é executado em segundo plano para fazer a requisição HTTP
        @Override
        protected String doInBackground(String... params) {
            String cep = params[0];
            String apiUrl = "https://viacep.com.br/ws/" + cep + "/json/";
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5 segundos de timeout para conexão
                connection.setReadTimeout(5000);    // 5 segundos de timeout para leitura

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) { // Código 200: Sucesso
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {
                    // Se houver erro na requisição (ex: 404, 500)
                    return null; // Indica que houve um erro
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null; // Retorna null em caso de exceção
            }
            return response.toString();
        }

        // Este método é executado na thread principal (UI thread) após doInBackground
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    // Verificar se a API retornou um erro (CEP não encontrado)
                    if (jsonObject.has("erro") && jsonObject.getBoolean("erro")) {
                        Toast.makeText(MainActivity.this, "CEP não encontrado. Verifique o número digitado.", Toast.LENGTH_LONG).show();
                        // Limpar os campos de resultado
                        textViewLogradouro.setText("Logradouro: ");
                        textViewBairro.setText("Bairro: ");
                        textViewLocalidade.setText("Localidade: ");
                        textViewUF.setText("UF: ");
                    } else {
                        // Extrair os dados do JSON e atualizar os TextViews
                        String logradouro = jsonObject.optString("logradouro", "Não informado");
                        String bairro = jsonObject.optString("bairro", "Não informado");
                        String localidade = jsonObject.optString("localidade", "Não informado");
                        String uf = jsonObject.optString("uf", "Não informado");

                        textViewLogradouro.setText("Logradouro: " + logradouro);
                        textViewBairro.setText("Bairro: " + bairro);
                        textViewLocalidade.setText("Localidade: " + localidade);
                        textViewUF.setText("UF: " + uf);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Erro ao processar os dados do CEP.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Erro ao consultar o CEP. Verifique sua conexão ou tente novamente.", Toast.LENGTH_LONG).show();
            }
        }
    }
}