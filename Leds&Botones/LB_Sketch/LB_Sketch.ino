/*********************************************************************
* Creado por: Fredy A. Cáceres Ramos
* Fecha de creación de Sketch: 27/Julio/2016
* Última modificación del Sketch: 29/Julio/2016
* Practica #1
*********************************************************************/

/*
 * Referencias 
 * https://geekytheory.com/tutorial-java-arduino-javaduino/
 * http://digitalyquetal.blogspot.com/2013/08/puerto-serial-arduino-en-linux.html
 */

const int red = 13;
const int green = 12;
const int blue = 11;
const int white = 10;
int lect = 0;

void setup() {
  Serial.begin(9600);
  pinMode(red, OUTPUT);
  pinMode(green, OUTPUT);
  pinMode(blue, OUTPUT);
  pinMode(white, OUTPUT);
  digitalWrite(red, LOW);
  digitalWrite(green, LOW);
  digitalWrite(blue, LOW);
  digitalWrite(white, LOW);
}

void loop() {
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
}
