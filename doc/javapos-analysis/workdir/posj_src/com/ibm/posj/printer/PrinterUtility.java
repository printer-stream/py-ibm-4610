package com.ibm.posj.printer;

import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.FileUtil;
import com.ibm.jutil.IntBuffer;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.util.BmpImageProducer;
import com.ibm.posj.util.PrintBitmapBWImageFilter;
import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.ImagingOpException;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PrinterUtility {
   public static final String X_DEAD = "The connection to X server has been lost";
   ImageProducer iProducer = new BmpImageProducer("");
   private static final String STR_FILE = "file";
   private static final String STR_SEP = ":";
   private static final int PRT_BYTE_FACTOR = 8;
   private static final int PRT_DEF_ONE = 1;
   private static final int PRT_DEF_INIT = 0;
   private static final int PRT_DEF_NOT_SET = -1;
   private static final int PRT_IMG_HDR_SIZE = 0;
   private static final int PRT_IMG_ID = 0;
   private static final String STR_ERROR_MEMORY = "Out of memory, the image could not be loaded";
   private static final String STR_ERROR_FORMAT = "Invalid image format";
   private static final String STR_ERROR_EXIST = "The image file does not exist";

   public Image loadFileImage(String fileName, boolean useBWFilter, Container container) throws ImagingOpException, IllegalArgumentException, IllegalAccessException {
      Image image = null;

      try {
         URL location = new URL(fileName);
         String protocol = location.getProtocol();
         if (protocol.equalsIgnoreCase("file")) {
            fileName = location.getHost() + ":" + location.getFile();
         }
      } catch (MalformedURLException var8) {
      }

      try {
         return this.loadImageFromDisk(fileName, useBWFilter, container);
      } catch (Error var7) {
         Tracer.getInstance().print(1, var7);
         throw new IllegalAccessException(var7.getLocalizedMessage());
      }
   }

   public Image loadImageFromDisk(String fileName, boolean useBWFilter, Container container) throws ImagingOpException, IllegalArgumentException, IllegalAccessException {
      File nameCheck = new File(fileName);
      Image image = null;
      URL location = null;
      boolean inJarFile = false;

      try {
         location = new URL(fileName);
      } catch (MalformedURLException var13) {
      }

      if ((location == null || location.getProtocol().equalsIgnoreCase("file")) && !nameCheck.exists()) {
         try {
            if (FileUtil.loadFile(fileName, true, true) != null) {
               inJarFile = true;
            }
         } catch (IOException var12) {
         }

         if (!inJarFile) {
            throw new IllegalArgumentException("The image file does not exist");
         }
      }

      try {
         ((BmpImageProducer)this.iProducer).setFile(fileName);
         image = Toolkit.getDefaultToolkit().createImage(this.iProducer);
         this.waitForImage(image, container);
      } catch (Exception var14) {
         if (location != null && !location.getProtocol().equalsIgnoreCase("file")) {
            try {
               image = Toolkit.getDefaultToolkit().createImage(location);
               this.waitForImage(image, container);
            } catch (Exception var11) {
               throw new IllegalArgumentException("The image file does not exist");
            }
         } else if (inJarFile) {
            try {
               image = Toolkit.getDefaultToolkit().createImage(FileUtil.findURL(fileName));
               this.waitForImage(image, container);
            } catch (FileNotFoundException var10) {
            }
         } else {
            image = Toolkit.getDefaultToolkit().getImage(fileName);
            this.waitForImage(image, container);
         }
      }

      if (useBWFilter) {
         PrintBitmapBWImageFilter bwFilter = new PrintBitmapBWImageFilter();
         image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), bwFilter));
         this.waitForImage(image, container);
      }

      return image;
   }

   public byte[] getImageStream(Image image, Container container, byte station, ImageStreamProducer streamProducer) throws ImagingOpException {
      int iWidth = image.getWidth(container);
      int iHeight = image.getHeight(container);
      IntBuffer pixels = IntBuffer.getIntBufferFactory().createIntBuffer(iWidth * iHeight);
      pixels.setIntCount(iWidth * iHeight, false);
      PixelGrabber grabber = new PixelGrabber(image, 0, 0, -1, -1, pixels.getIntRef(), 0, iWidth);

      try {
         grabber.grabPixels();
      } catch (InterruptedException var11) {
         throw new ImagingOpException("Invalid image format");
      }

      streamProducer.setImageValues(0L, 0L, (long)iWidth, (long)iHeight, pixels.getIntRef());
      byte[] bmpStream = ByteArrayCollector.getCollector().getArray(this.getImageByteSize(iWidth, iHeight));
      int bmpLoc = 0;

      while (!streamProducer.streamDone()) {
         bmpStream[bmpLoc++] = (byte)streamProducer.getNextByteForStation(station);
      }

      streamProducer.freeISP();
      pixels.recycle();
      return bmpStream;
   }

   public int getBytseWidth(int width) {
      return (width - 1) / 8 + 1;
   }

   public int getBytesHeight(int height) {
      return (height - 1) / 8 + 1;
   }

   public int getBlockBytesHeight(int width, int maxImageSize) {
      return maxImageSize / (width * 8);
   }

   public int getImageByteSize(int width, int height) {
      return this.getBytseWidth(width) * this.getBytesHeight(height) * 8 + 0;
   }

   public Image scaleImage(Image image, Container container, int newWidth, int maxWidth, int maxHeight, int maxImageSize) throws IllegalAccessException, ImagingOpException {
      int oldWidth = image.getWidth(container);
      float ratio = (float)image.getHeight(container) / (float)oldWidth;
      int newHeight = (int)((float)newWidth * ratio);
      if (oldWidth != newWidth) {
         image = image.getScaledInstance(newWidth, newHeight, 1);
         this.waitForImage(image, container);
      }

      return image;
   }

   public Image scale4689Image(Image image, Container container, int newWidth, int maxWidth, int maxHeight, int maxImageSize) throws IllegalAccessException, ImagingOpException {
      int oldWidth = image.getWidth(container);
      if (oldWidth != newWidth) {
         image = image.getScaledInstance(newWidth, maxHeight, 1);
         this.waitForImage(image, container);
      }

      return image;
   }

   public void waitForImage(Image image, Container container) throws IllegalAccessException, ImagingOpException {
      MediaTracker tracker = null;

      try {
         tracker = new MediaTracker(container);
         tracker.addImage(image, 0);
         tracker.waitForID(0);
      } catch (InterruptedException var5) {
      } catch (OutOfMemoryError var6) {
         throw new IllegalAccessException("Out of memory, the image could not be loaded");
      }

      if (tracker.isErrorAny()) {
         throw new ImagingOpException("Invalid image format");
      }
   }

   public int convertDotsToFeedUnits(int dots, byte station, PrinterHandleState handleState) {
      float inches = (float)dots / (float)handleState.getDotsPerInchHigh(station);
      return (int)(inches * (float)handleState.getFeedStepsPerInch(station));
   }
}
