int a1 = 3;
int b1 = 5;
int a2 = 6;
int b2 = 9;

void setup() {
  // put your setup code here, to run once:
  pinMode(a1,OUTPUT);
  pinMode(b1,OUTPUT);
  pinMode(a2,OUTPUT);
  pinMode(b2,OUTPUT);
Serial.begin(9600);


}

void loop() {
  // put your main code here, to run repeatedly:

  
  if(Serial.available()){
    int i = Serial.read()-48;
    if(i==1){
      //Serial.println("forward");
      moveForward();
    }
    
    else if (i==2){
      //Serial.println("right");
      spinRight();
    }
    
    else if (i==3){
      //Serial.println("left");
     spinLeft();
    }
        
    else{
      //Serial.println("backward");
      moveBackward();
    }      
  }
 }
 
void moveForward(){
   digitalWrite(a1,LOW);
   digitalWrite(b1,HIGH);
   digitalWrite(a2,HIGH);
   digitalWrite(b2,LOW);
   //delay(25);
   digitalWrite(a1,LOW);
   digitalWrite(b1,LOW);
   digitalWrite(a2,LOW);
   digitalWrite(b2,LOW);
 }
 
 void moveBackward(){
   digitalWrite(a1,HIGH);
   digitalWrite(b1,LOW);
   digitalWrite(a2,LOW);
   digitalWrite(b2,HIGH);
    //delay(25);
      digitalWrite(a1,LOW);
   digitalWrite(b1,LOW);
   digitalWrite(a2,LOW);
   digitalWrite(b2,LOW);
 }
 
 void spinRight(){
   digitalWrite(a1,LOW);
   digitalWrite(b1,HIGH);
   digitalWrite(a2,LOW);
   digitalWrite(b2,HIGH);
   //delay(25);
     digitalWrite(a1,LOW);
   digitalWrite(b1,LOW);
   digitalWrite(a2,LOW);
   digitalWrite(b2,LOW);
 }
 
 void spinLeft(){
   digitalWrite(a1,HIGH);
   digitalWrite(b1,LOW);
   digitalWrite(a2,HIGH);
   digitalWrite(b2,LOW);
   //delay(25);
   digitalWrite(a1,LOW);
   digitalWrite(b1,LOW);
   digitalWrite(a2,LOW);
   digitalWrite(b2,LOW);
 }


