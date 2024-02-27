import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage framedImg;
	BufferedImage transformedImg;
	String imgPath;
	int updatedPix = 262144;



	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
 	int height = 512;
	double zoomInc, thetaInc;
	int fpsCnt=0;
	double theta, zoom, fps;
	Timer timer;
	boolean endRend = false;
	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}

	public void showIms(String[] args){

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgPath = args[0];
		readImageRGB(width, height, imgPath, imgOne);
		framedImg = imgOne;
		// readMyImageRGB(width, height, args[0], imgOne, Float.parseFloat(args[1]), Float.parseFloat(args[2]));

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);

		zoom = Double.parseDouble(args[1]);
		theta = Math.toRadians(Double.parseDouble(args[2]));
		fps = Integer.parseInt(args[3]);

		thetaInc = theta / fps;
		zoomInc = (zoom-1)/fps;
		timer = new Timer();
        TimerTask task = new TimerTask(){
			public void run(){
				// if(Float.parseFloat(args[1]) >=1){
				// 	args[1]=""+Float.parseFloat(args[1])*2+"";
				// }
				// else{
				// 	args[1]=""+Float.parseFloat(args[1])/2+"";
				// }
				// args[2]=""+Float.parseFloat(args[2])*2+"";
				// ren.showIms(args);
				if(!endRend){
					BufferedImage framedImg = imgOne;
					framedImg = readMyImageRGB(imgOne, zoomInc, thetaInc, fpsCnt);
					lbIm1.setIcon(new ImageIcon(framedImg));
					fpsCnt++;
				}
			}
		};

		timer.scheduleAtFixedRate(task, 0,(int) (1000/fps));
		// args[1]=""+Float.parseFloat(args[1])*2+"";
		// args[2]=""+Float.parseFloat(args[2])*2+"";
		// Timer timer = new Timer();
        // TimerTask task = new TimerTask(){
		// 	public void run(){
		// 	showIms(args);
		// 	}
		// };

		// timer.scheduleAtFixedRate(task, 1000, 1000);

	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
		
		// Timer timer = new Timer();
        // TimerTask task = new TimerTask(){
		// 	public void run(){
		// 		if(Float.parseFloat(args[1]) >=1){
		// 			args[1]=""+Float.parseFloat(args[1])*2+"";
		// 		}
		// 		else{
		// 			args[1]=""+Float.parseFloat(args[1])/2+"";
		// 		}
		// 		args[2]=""+Float.parseFloat(args[2])*2+"";
		// 		ren.showIms(args);
		// 	}
		// };

		// timer.scheduleAtFixedRate(task, 1000, 1000);
	}



	//My Code
	public BufferedImage readMyImageRGB(BufferedImage img, double zoomInc, double thetaInc, int fpsCnt) {
    try {
        int frameLength = width * height * 3;

        File file = new File(imgPath);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(0);

        long len = frameLength;
        byte[] bytes = new byte[(int) len];

        raf.read(bytes);
		
		int prevUpdatedPix = updatedPix;
		updatedPix = 0;
		int newPix;

		double z = 1 + (fpsCnt * zoomInc);
		double t = fpsCnt * thetaInc;
		// System.out.println(z);

        double[] stamat = statmat(z, t);
		transformedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// prevUpdatedPix = updatedPix;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] xy_ori = new double[]{x - (width / 2), y - (height / 2)};
                double[] xy_trans = dynamat(xy_ori, stamat);
                int x_new =(int) Math.round((xy_trans[0] + (width / 2)));
                int y_new =(int) Math.round((xy_trans[1] + (height / 2)));
				int pix;

                if (x_new >= 0 && x_new < width && y_new >= 0 && y_new < height) {
                    int ind = y_new * width + x_new;
					if(zoom<1){
						pix = antiAliasingFilter(x_new, y_new);
						updatedPix++;
					}
					else{
                    	byte a = 0;
                    	byte r = bytes[ind];
                    	byte g = bytes[ind + height * width];
                    	byte b = bytes[ind + height * width * 2];

                    	pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					}
					// prevUpdatedPix = updatedPix;
					// updatedPix++;
                    transformedImg.setRGB(x, y, pix);
                }
            }
        }
		

		if(zoom<1 && prevUpdatedPix < updatedPix){
			endRend = true;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{

					// byte a = 0;
					// byte r = 0;
					// byte g = 0;
					// byte b = 0;

					int pix = 0xff000000; //| ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					transformedImg.setRGB(x,y,pix);
				}
			}
			
		}

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
	return transformedImg;
	// readMyImageRGB(width, height, imgPath, img, z*2, theta*2);

}

	public int antiAliasingFilter(int x_ori,int y_ori){
		int rSum=0, gSum=0, bSum=0, cnt=0;
		for(int i=-1; i<=1; i++){
			for(int j=-1; j<=1; j++){
				int xx = Math.min(Math.max(x_ori + i, 0), width - 1);
				int yy = Math.min(Math.max(y_ori + j, 0), height - 1);

				int pixel = imgOne.getRGB(xx, yy);
				rSum += (pixel >> 16) & 0xFF;
				gSum += (pixel >> 8) & 0xFF;
				bSum += pixel & 0xFF;
				cnt++;
			}
		}

		int rAvg = rSum / cnt;
		int gAvg = gSum / cnt;
		int bAvg = bSum / cnt;
		return (rAvg << 16) | (gAvg << 8) | bAvg;

	}

	public static double[] statmat(double zscale, double angle){
        double retmat[] = new double[4];
        // double r = Math.toRadians(angle);
        retmat[0] = (1/zscale)*Math.cos(angle);
        retmat[1] = (1/zscale)*Math.sin(angle);
        retmat[2] = -(1/zscale)*Math.sin(angle);
        retmat[3] = (1/zscale)*Math.cos(angle);
        return retmat;
    }

	public static double[] dynamat(double[] xy_ori, double[] mat){
        double retmat[]= new double[2];
        retmat[0] = xy_ori[0]*mat[0] + xy_ori[1]*mat[1];
        retmat[1] = xy_ori[0]*mat[2] + xy_ori[1]*mat[3];
        return retmat;
    }

	//My End
}