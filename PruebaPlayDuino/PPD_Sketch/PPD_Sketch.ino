/*********************************************************************
* Creado por: Fredy A. Cáceres Ramos
* Fecha de creación de Sketch: 29/Julio/2016
* Última modificación del Sketch: 29/Julio/2016
* Sketch de prueba para el PlayDuino v0.2
*********************************************************************/

/*
 * Referencias 
 * https://linuxgx.blogspot.com/2014/06/recibir-datos-desde-arduino-utilizando.html
 */

/*
 * Constantes
 */
#define red 13
#define green 12
#define blue 11
#define white 10

#define btn1 7
#define btn2 8
#define btn3 9
#define btn4 A0

/*
 * Variables
 */
int lect = 0;
int pot = 0;
String cadena = "";

/*
 * MUX3  MUX2  MUX1  MUX0  Analog Pin
 *  0     0     0     0       0
 *  0     0     0     1       1
 *  0     0     1     0       2
 *  0     0     1     1       3
 *  0     1     0     0       4
 *  0     1     0     1       5
 */

void setup() {
  Serial.begin(9600);

  bitWrite(ADCSRA,ADPS2,1);
  bitWrite(ADCSRA,ADPS1,0);
  bitWrite(ADCSRA,ADPS0,0);

  pinMode(red, OUTPUT);
  pinMode(green, OUTPUT);
  pinMode(blue, OUTPUT);
  pinMode(white, OUTPUT);
  pinMode(btn1, INPUT);
  pinMode(btn2, INPUT);
  pinMode(btn3, INPUT);
  pinMode(btn4, INPUT);
  digitalWrite(red, LOW);
  digitalWrite(green, LOW);
  digitalWrite(blue, LOW);
  digitalWrite(white, LOW);
}

void loop() {
  /*
   * Sección de los leds del PlayDuino
   * Recibe de la intefaz de Java los estados de los botones
   */
  if(Serial.available() > 0) {
    lect = Serial.read();
    if(lect == '0')
      digitalWrite(red, HIGH);
    else if(lect == '1')
      digitalWrite(red, LOW);
    else if(lect == '2')
      digitalWrite(green, HIGH);
    else if(lect == '3')
      digitalWrite(green, LOW);
    else if(lect == '4')
      digitalWrite(blue, HIGH);
    else if(lect == '5')
      digitalWrite(blue, LOW);
    else if(lect == '6')
      digitalWrite(white, HIGH);
    else if(lect == '7')
      digitalWrite(white, LOW);
  }

  /*
   * Sección de los potenciometros
   * Envia los valores leidos de cada potenciometro a la interfaz Java
   */
  pinAnalogoLectura(1,1,0,0); //Ponemos el analogico 3 como primera lectura
  pinAnalogoLectura(0,0,1,0); //Ponemos el analogico 4 como primera lectura
  pinAnalogoLectura(1,0,1,0); //Ponemos el analogico 5 como primera lectura
  
  /*
   * Seccón de los botones
   * Envia los estados de cada boton a la interfaz Java
   */
   aCad(btn1);
   aCad(btn2);
   aCad(btn3);
   aCad(btn4);
   Serial.println(cadena);
   cadena = "";
}

void aCad(int btn) {
   if (digitalRead(btn) == HIGH) {
    cadena += "off,";
   } else {
    cadena += "on,";
   }
   delay(100);
}

void pinAnalogoLectura(int m0, int m1, int m2, int m3) {
  // Definiendo el puerto analogico A5 como el puerto de lectura de datos
  // Leer en el enlace para mayor referencia
  ADMUX=(1<<ADLAR)|(0<<REFS1)|(1<<REFS0)|(m3<<MUX3)|(m2<<MUX2)|(m1<<MUX1)|(m0<<MUX0);
  pot = analogReadFast();
  cadena += (String) pot + ",";
  delay(100);
}

/*
 * Función utilizada para realizar la lectura de datos de una manera mas rápida, basado en el segundo enlace de referencia.
 */
int analogReadFast() {
 ADCSRA|=(1<<ADSC);
 while (bit_is_set(ADCSRA, ADSC)); // Se limpia el ADSC cuando termina la conversión
        return ADCH;
}
