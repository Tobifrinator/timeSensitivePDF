import java.awt.Color;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties.BaseState;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;

public class createTimeSensitivePDF {

   public static void CreateBasePDF(String path) throws Exception{
      PDDocument document= new PDDocument();
      PDPage page= new PDPage(PDRectangle.A4);
      document.addPage(page);
      PDPageContentStream cStream = new PDPageContentStream(document, page);

      //Add text
      cStream.beginText();
      float a4fontSize=14;
      cStream.setFont(PDType1Font.COURIER, a4fontSize);
      cStream.setNonStrokingColor(Color.BLACK);
      cStream.newLineAtOffset(page.getTrimBox().getLowerLeftX()+15, page.getTrimBox().getUpperRightY()-a4fontSize-15);
      cStream.showText("This document changes content depending on the time.");
      cStream.newLineAtOffset(0f, -a4fontSize*1.5f);
      cStream.showText("The content changes on 7:00 and 18:00.");
      cStream.newLineAtOffset(0f, -a4fontSize*1.5f);
      cStream.showText("The signature is not invalidated by this change.");
      cStream.endText();

      //Add images for day and night
      PDImageXObject sun = PDImageXObject.createFromFile("assets\\sonne.png", document);
      cStream.drawImage(sun, 0, 0, PDRectangle.A4.getWidth(), (PDRectangle.A4.getWidth()/sun.getWidth())*sun.getHeight());;
      PDImageXObject moon = PDImageXObject.createFromFile("assets\\mond.png", document);
      cStream.drawImage(moon, 0, 0, PDRectangle.A4.getWidth(), (PDRectangle.A4.getWidth()/moon.getWidth())*moon.getHeight());;

      //Add images to different Optional Content Groups
      PDOptionalContentGroup day = new PDOptionalContentGroup("day");
      sun.setOptionalContent(day);
      PDOptionalContentGroup night = new PDOptionalContentGroup("night");
      moon.setOptionalContent(night);

      //Set all OptionalContentGroups to hidden
      PDOptionalContentProperties ocprop = new PDOptionalContentProperties();
      ocprop.setBaseState(BaseState.OFF);
      ocprop.addGroup(day);
      ocprop.addGroup(night);
      document.getDocumentCatalog().setOCProperties(ocprop);
      cStream.close();

      //Add javascript to change content dependant on time
      PDActionJavaScript PDAjavascript = new PDActionJavaScript("var d=new Date(); if(d.getHours()<=17 && d.getHours()>=7){this.getOCGs()[0].state=true;}else{this.getOCGs()[1].state=true;}");
      document.getDocumentCatalog().setOpenAction(PDAjavascript);

      //Save the document
      document.save(path);
      document.close();
   }

   public static void main(String args[]) throws Exception{
      var path="TimeSensitivePDF.pdf";
      CreateBasePDF(path);
      //AddInteraction(path);
      System.out.println("Created PDF");
   }
}