package charlesgunn.video;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.swing.JFrame;

import jmapps.jmstudio.CaptureControlsDialog;
import jmapps.jmstudio.CaptureDialog;
import jmapps.util.CDSWrapper;
import jmapps.util.JMFUtils;

/*
 * Created on 31.12.2005
 *
 * This file is part of the  package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class AbstractVideoProvider  implements BufferTransferHandler {

    private String nameCaptureDeviceVideo;
    private PushBufferDataSource dataSourceCurrent = null;
    private CaptureControlsDialog dlgCaptureControls = null;
    private Frame frame;
    FrameGrabbingControl grabberControl;
//    private BufferToImage converter;
    private PushBufferStream stream;
    protected Buffer buffer = new Buffer(); 
    
     public AbstractVideoProvider(Frame f) {
        super();
        this.frame = f;
        captureMedia();
        // TODO Auto-generated constructor stub
    }  

    public void open( DataSource dataSource) {
        
            if (dataSource instanceof PushBufferDataSource) {
                    dataSourceCurrent = (PushBufferDataSource) dataSource;
                    System.out.println(""+dataSource+ " is push buffer");
                    try {
                        dataSource.connect();
                        PushBufferStream[] strs = dataSourceCurrent.getStreams();
                        stream = strs[0];
//                        System.out.println("frame rate is "+((RGBFormat)(stream.getFormat()).ge)
                        stream.setTransferHandler(this);
                        dataSource.start();
 
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            
        }
    
        
        
    private void captureMedia () {
        CaptureDialog       dialogCapture;
        DataSource          dataSource;
        CaptureDeviceInfo   cdInfo;

        nameCaptureDeviceVideo = null;

        dialogCapture = new CaptureDialog ( frame, null);
        dialogCapture.show ();
        if (dialogCapture.getAction() == CaptureDialog.ACTION_CANCEL)
            return;

        cdInfo = dialogCapture.getVideoDevice();
        if ( cdInfo != null )
            nameCaptureDeviceVideo = cdInfo.getName();
        dataSource = JMFUtils.createCaptureDataSource ( null,
                                                dialogCapture.getAudioFormat(),
                                                nameCaptureDeviceVideo,
                                                dialogCapture.getVideoFormat() );
        System.out.println("dataSource "+dataSource); 
        if ( dataSource != null ) {

            if (dataSource instanceof CaptureDevice
                            &&  dataSource instanceof PushBufferDataSource) {
                DataSource cdswrapper = new CDSWrapper((PushBufferDataSource)dataSource);
                dataSource = cdswrapper;
                try {
                    cdswrapper.connect();
                }
                catch (IOException ioe) {
                    dataSource = null;
                   
                    nameCaptureDeviceVideo = null;
                    ioe.printStackTrace();
                }
            }

            open ( dataSource );
            if ( dataSource != null ) {
                dlgCaptureControls = new CaptureControlsDialog (  frame, dataSource );
                if ( dlgCaptureControls.isEmpty() ) {
                    dlgCaptureControls = null;
                }
                else {
//                    dlgCaptureControls.setVisible ( true );
                }
            }
        }
        else {
            
            System.err.println("error..."+nameCaptureDeviceVideo);
            nameCaptureDeviceVideo = null;
        }
    }


    
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
            if(dataSourceCurrent!= null) dataSourceCurrent.stop();
        super.finalize();
    }
    
    
    public final void transferData(PushBufferStream arg0) {
        try {
            stream.read(buffer);
            processBuffer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    long t = System.currentTimeMillis();
    protected final void processBuffer() {
        if(System.currentTimeMillis()-t >1000/5 ) {
	        t = System.currentTimeMillis();
	        Format format = buffer.getFormat();
	        if(format instanceof RGBFormat) {
	            RGBFormat rgb = (RGBFormat) format;
	            Dimension d = rgb.getSize();
	            byte[] b = (byte[]) buffer.getData();
	            System.err.println("RGB format = "+rgb.toString());
	//            width = d.width;
	//            height = d.height;
	//            ba = b;
	//            newData  = true;
	        }
        }
   }
    
    public static void main(String[] args)	{
    	JFrame frame = new JFrame("test");
    	frame.setPreferredSize(new Dimension(200, 200));
    	frame.pack();
    	frame.setVisible(true);
    	AbstractVideoProvider avp = new AbstractVideoProvider(frame);
    }
}
