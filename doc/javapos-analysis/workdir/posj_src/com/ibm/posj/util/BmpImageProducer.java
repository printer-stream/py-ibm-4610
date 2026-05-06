package com.ibm.posj.util;

import com.ibm.jutil.IntBuffer;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class BmpImageProducer implements ImageProducer {
   private String spec = null;
   private ImageProducer ip = null;
   private Vector consumers = new Vector();
   IntBuffer pixels = null;

   public BmpImageProducer(String spec) {
      this.spec = spec;
   }

   public void setFile(String spec) {
      this.spec = spec;
   }

   public synchronized void addConsumer(ImageConsumer ic) {
      if (!this.consumers.contains(ic)) {
         this.consumers.add(ic);

         try {
            this.ip.addConsumer(ic);
         } catch (Exception var3) {
         }
      }
   }

   public synchronized boolean isConsumer(ImageConsumer ic) {
      return this.consumers.contains(ic);
   }

   public synchronized void removeConsumer(ImageConsumer ic) {
      this.consumers.remove(ic);

      try {
         this.ip.removeConsumer(ic);
      } catch (Exception var3) {
      }
   }

   public synchronized void requestTopDownLeftRightResend(ImageConsumer ic) {
      try {
         this.getImageProducer().requestTopDownLeftRightResend(ic);
      } catch (Exception var3) {
         ic.imageComplete(1);
      }
   }

   public synchronized void startProduction(ImageConsumer ic) {
      try {
         this.getImageProducer(ic).startProduction(ic);
      } catch (Exception var4) {
         Enumeration e = this.consumers.elements();

         while (e.hasMoreElements()) {
            ((ImageConsumer)e.nextElement()).imageComplete(1);
         }
      }
   }

   private ImageProducer getImageProducer(ImageConsumer ic) {
      if (ic != null && !this.consumers.contains(ic)) {
         this.consumers.add(ic);
      }

      return this.getImageProducer();
   }

   private ImageProducer getImageProducer() {
      if (this.ip == null) {
         BmpImageProducer.BmpInputStream in = null;

         try {
            try {
               in = new BmpImageProducer.BmpInputStream(new URL(this.spec).openStream());
            } catch (MalformedURLException var19) {
               in = new BmpImageProducer.BmpInputStream(new FileInputStream(this.spec));
            }

            if (in.readChar() != 'B' || in.readChar() != 'M') {
               throw new Exception("Not a BMP");
            }

            in.skip(16L);
            int width = in.readInt();
            int height = in.readInt();
            in.skip(2L);
            int bitsPerPixel = in.readShort();
            int compressionType = in.readInt();
            in.skip(20L);
            int length = 1 << bitsPerPixel;
            IntBuffer colorMap = IntBuffer.getIntBufferFactory().createIntBuffer(length);

            for (int i = 0; i < length; i++) {
               colorMap.append(in.readInt() | 0xFF000000);
            }

            if (compressionType != 0) {
               throw new Exception("Unsupported compression type");
            }

            if (bitsPerPixel < 1 || bitsPerPixel > 8) {
               throw new Exception("Unsupported color depth");
            }

            if (this.pixels == null || this.pixels.getIntRef().length < height * width) {
               this.pixels = IntBuffer.getIntBufferFactory().createIntBuffer(height * width);
            }

            int pixelsPerByte = 8 / bitsPerPixel;
            int bytesPerRow = width / pixelsPerByte + (width % pixelsPerByte != 0 ? 1 : 0);
            int pad = bytesPerRow % 4 > 0 ? 4 - bytesPerRow % 4 : 0;
            int mask = (1 << bitsPerPixel) - 1;
            IntBuffer shift = IntBuffer.getIntBufferFactory().createIntBuffer(pixelsPerByte);

            for (int i = 0; i < pixelsPerByte; i++) {
               shift.append(8 - (i + 1) * bitsPerPixel);
            }

            this.pixels.setIntCount(height * width, false);
            int bitIndex = 0;
            int imageByte = 0;
            int value = 0;

            for (int y = height - 1; y >= 0; y--) {
               int i = y * width;

               for (int x = 0; x < width; x++) {
                  if (x % pixelsPerByte == 0) {
                     bitIndex = 0;
                     imageByte = in.read();
                  }

                  value = colorMap.intAt(imageByte >> shift.intAt(bitIndex++) & mask);
                  this.pixels.setInt(value, i++);
               }

               in.skip((long)pad);
            }

            this.ip = new MemoryImageSource(width, height, this.pixels.getIntRef(), 0, width);
            colorMap.recycle();
            shift.recycle();
         } catch (Exception var20) {
         }

         if (this.ip != null) {
            Enumeration e = this.consumers.elements();

            while (e.hasMoreElements()) {
               this.ip.addConsumer((ImageConsumer)e.nextElement());
            }
         }
      }

      return this.ip;
   }

   private class BmpInputStream extends BufferedInputStream {
      public BmpInputStream(InputStream in) {
         super(in);
      }

      public int readInt() throws IOException {
         return this.read() & 0xFF | (this.read() & 0xFF) << 8 | (this.read() & 0xFF) << 16 | (this.read() & 0xFF) << 24;
      }

      public short readShort() throws IOException {
         return (short)(this.read() & 0xFF | (this.read() & 0xFF) << 8);
      }

      public char readChar() throws IOException {
         return (char)(this.read() & 0xFF);
      }
   }
}
