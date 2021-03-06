/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.kvstore

import com.larskroll.common.repl._
import com.typesafe.scalalogging.StrictLogging;
import org.apache.log4j.Layout
import util.log4j.ColoredPatternLayout;
import fastparse._, NoWhitespace._
import concurrent.Await
import concurrent.duration._

object ClientConsole {
  def lowercase[_: P] = P(CharsWhileIn("a-z"))
  def uppercase[_: P] = P(CharsWhileIn("A-Z"))
  def digit[_: P] = P(CharsWhileIn("0-9"))
  def simpleStr[_: P] = P(lowercase | uppercase | digit)
  val colouredLayout = new ColoredPatternLayout("%d{[HH:mm:ss,SSS]} %-5p {%c{1}} %m%n");
}

class ClientConsole(val service: ClientService) extends CommandConsole with ParsedCommands with StrictLogging {
  import ClientConsole._;

  override def layout: Layout = colouredLayout;
  override def onInterrupt(): Unit = exit();

  val opParser = new ParsingObject[String] {
    override def parseOperation[_: P]: P[String] = P("op" ~ (" " ~ simpleStr).rep.!);
  }

  val opCommand = parsed(opParser, usage = "op <key>", descr = "Executes an op for <key>.") { key =>
    println(s"Input for Op: $key");
    
    var input: Array[String] = key.split(" ")
    var operationType = input(1).toUpperCase()
    var size = input.length
    
   if(size < 3){ 
   println("Too few input fields provided");
   }
   else{
   
    var keyV = input(2)
     
     if(size == 3 && operationType == "GET"){
       
       val fr = service.op(Op(operationType, keyV, " ", " "));
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status + " Result was: " + r.value);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
     
        
   
   }
   
   if(size == 4 && operationType == "PUT"){
     
     var valuePut = input(3)
     
     val fr = service.op(Op(operationType, keyV, valuePut, " ")); 
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status + " Result was: " + r.value);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
   
   }
   
   if(size == 5 && operationType == "CAS"){
     
     var valueCas = input(4)
     var expectedCas = input(3)
     
     val fr = service.op(Op(operationType, keyV, valueCas, expectedCas)); 
    out.println("Operation sent! Awaiting response...");
    try {
      val r = Await.result(fr, 5.seconds);
      out.println("Operation complete! Response was: " + r.status + " Result was: " + r.value);
    } catch {
      case e: Throwable => logger.error("Error during op.", e);
    }
   
   }
    
     
   }
    
  };

}

