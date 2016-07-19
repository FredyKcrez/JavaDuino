const int red = 13;
const int green = 12;
int lect = 0;

void setup() {
  Serial.begin(9600);
  pinMode(red, OUTPUT);
  pinMode(green, OUTPUT);
  digitalWrite(red, LOW);
  digitalWrite(green, LOW);
}

void loop() {
  if(Serial.available() == 0) {
    lect = Serial.read();
    if(lect == '0')
      digitalWrite(red, HIGH);
    else if(lect == '1')
      digitalWrite(green, HIGH);
  }
  digitalWrite(red, LOW);
  digitalWrite(green, LOW);
}
