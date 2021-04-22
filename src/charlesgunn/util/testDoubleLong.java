package charlesgunn.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class testDoubleLong extends Object {

    public static void main(String args[]) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        long hh, ll;
        double a, b;
        doubleLong dl1, dl2, dl3;
        dl1 = new doubleLong(1.0);
        dl2 = new doubleLong();
        dl3 = new doubleLong();
        while(dl1.doubleValue() != 0.0)	{
            try {
                System.out.print("Enter a:  ");
                String line = in.readLine();
                dl1 = new doubleLong(a = Double.parseDouble(line));
                dl2 = new doubleLong(dl1.doubleValue());
				System.out.println("Check: Double value is: "+dl2.doubleValue());
                System.out.print("Enter b:  ");
                line = in.readLine();
                dl2 = new doubleLong(b = Double.parseDouble(line));
                System.out.println("a is: "+dl1.toBinaryString());
                System.out.println("b is: "+dl2.toBinaryString());
                //dl1.multiply(dl2);
               doubleLong.multiply(dl1, dl2, dl3);
				System.out.println("a is: "+dl1.toBinaryString());
				System.out.println("b is: "+dl2.toBinaryString());
                System.out.println(dl3.toBinaryString());
				System.out.println("Value is: "+dl3.doubleValue());
				System.out.println("Double version: "+(a*b));
				System.out.println("Check: Double version: "+(new doubleLong(a*b)).doubleValue());
            }
            catch (java.io.IOException e)	{
                System.out.println("IOException:"+e.getMessage());
            }
               
            }
        }
    
}
