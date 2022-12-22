package com.mas.name4;

import java.util.HashMap;
import java.util.Map;

public class Linear {

    //最小二乘
    private static double a;

    private static double b;

    private static int num;

    /**
     * 训练
     *
     * @param x
     * @param y
     */
    public static void train(double x[], double y[]) {
        num = x.length < y.length ? x.length : y.length;
        calCoefficientes(x,y);
    }

    /**
     * a=(NΣxy-ΣxΣy)/(NΣx^2-(Σx)^2)
     * b=y(平均)-a*x（平均）
     * @param x
     * @param y
     * @return
     */
    public static void calCoefficientes (double x[],double y[]){
        double xy=0.0,xT=0.0,yT=0.0,xS=0.0;
        for(int i=0;i<num;i++){
            xy+=x[i]*y[i];
            xT+=x[i];
            yT+=y[i];
            xS+=Math.pow(x[i], 2.0);
        }
        a= (num*xy-xT*yT)/(num*xS-Math.pow(xT, 2.0));
        b=yT/num-a*xT/num;
    }


    private double theta0 = 0.0 ;  //截距
    private double theta1 = 0.0 ;  //斜率


    //private double theta0 = 24.659215572062305;
    //private double theta1 = -0.3151917904159419;

    //private double theta1 = -0.44462380921721034;
    //private double theta0 = 20.351716990942986;

    void init() {
        this.theta0 = this.b;
        this.theta1 = this.a;
    }

    private double alpha = 0.0001 ;  //学习速率
    private double[] x = null;
    private double[] y = null;
    private double loss = 0;

    private boolean flag = true;

    private int max_itea = 200 ; //最大迭代步数

    public Linear(double[] x, double[] y) {
        this.x = x;
        this.y = y;
        train(x, y);
        init();
    }

    public double predict(double x){
        return theta0+theta1*x ;
    }

    public double calc_error(double x, double y) {
        return predict(x)-y;
    }


    public void gradientDescient(){
        double sum0 =0.0 ;
        double sum1 =0.0 ;

        for(int i = 0 ; i < x.length ;i++) {
            //sum0 += calc_error(x[i], y[i]) ;
            //sum1 += calc_error(x[i], y[i])*x[i] ;
            sum0 += calc_error(x[i], y[i])/y[i]/y[i];
            sum1 += calc_error(x[i], y[i])*x[i]/y[i]/y[i];
        }

        this.theta0 = theta0 - alpha*Math.max(Math.min(1, sum0/x.length), -1) ;
        this.theta1 = theta1 - alpha*Math.max(Math.min(1, sum1/x.length), -1) ;

        double sum2 = 0;
        for(int i = 0 ; i < x.length ;i++) {
            sum2 += Math.min(Math.abs(y[i]-predict(x[i]))/y[i],1);
        }
        this.loss = sum2/x.length;
    }

    public void lineGre() {
        int itea = 1 ;
        while( itea<= max_itea){
            if(itea>100&&flag) {
                alpha /= 5;
                flag = false;
            }
            //System.out.println(error_rate);
            gradientDescient();
            System.out.println("The current step is :"+itea);
            System.out.println("theta0 "+theta0);
            System.out.println("theta1 "+theta1);
            System.out.println("loss "+loss);
            System.out.println();
            itea ++ ;
        }
    } ;

    /*
    public static void main(String[] args){
        double[] x = {

                76.14404844,
                75.44677253,
                74.05817662,
                74.01384969,
                64.93145076,
                53.33592964,
                45.32225338,
                40.62907862,
                30.18355644

                //
                46.46566667,
                44.11233333,
                42.57,
                40.865,
                35.34566667,
                28.933,
                22.196,
                18.624,
                12.293
                //
        } ;
        double[] y = {
                0.1,
                0.5,
                1,
                2,
                5,
                8,
                10,
                12,
                15 } ;
        Linear linearRegression = new Linear(x, y) ;
        linearRegression.lineGre();


        List<Double> lx = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            lx = Arrays.stream(x).boxed().collect(Collectors.toList());
        }
        //lx.forEach(System.out::println);
        List<Double> ly = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ly = Arrays.stream(y).boxed().collect(Collectors.toList());
        }

        paint("Linear Model", lx, ly, linearRegression.theta0, linearRegression.theta1,linearRegression.loss);

    }
    */

    public static Map Cal(double[] x, double[] y){
        Linear linearRegression = new Linear(x, y) ;
        linearRegression.lineGre();

        Map<String, String> mp = new HashMap<>();
        mp.put("a",String.valueOf(linearRegression.theta1));
        mp.put("b",String.valueOf(linearRegression.theta0));
        mp.put("loss",String.valueOf(linearRegression.loss));

        return mp;
        /*
        List<Double> lx = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            lx = Arrays.stream(x).boxed().collect(Collectors.toList());
        }
        //lx.forEach(System.out::println);
        List<Double> ly = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ly = Arrays.stream(y).boxed().collect(Collectors.toList());
        }

         */


        //paint("Linear Model", lx, ly, linearRegression.theta0, linearRegression.theta1,linearRegression.loss);

    }

    /*
    public static void paint(String title,List<Double> x,List<Double> y, double theta0,double theta1,double loss){
        DefaultXYDataset   xydataset = new DefaultXYDataset ();

        for(double i=4 ; theta0 + theta1*i > 0.01 ; i+=0.01) {
            x.add(i);
            y.add(theta0 + theta1*i);
        }

        double[][] data=new double[2][x.size()];
        for(int i=0;i<y.size();i++)
        {
            data[0][i]=x.get(i);
            data[1][i]=y.get(i);
        }

        xydataset.addSeries("Linear Model", data);

        final JFreeChart chart =ChartFactory.createScatterPlot(title,"Gray Value","Drug Concentration",xydataset,PlotOrientation.VERTICAL,false,true,false);
        chart.addSubtitle(new TextTitle("MPE loss = "+(double)Math.round(loss * 10000) / 100+"%"));
        chart.setBackgroundPaint(ChartColor.WHITE);
        //chart.setBackgroundImageAlpha(0);
        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setBackgroundPaint(ChartColor.WHITE);
        XYDotRenderer xydotrenderer = new XYDotRenderer();
        xydotrenderer.setDotWidth(3);
        xydotrenderer.setDotHeight(3);
        xyplot.setRenderer(xydotrenderer);


        ChartFrame frame = new ChartFrame(title,chart);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);



        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("bar1.png");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            ChartUtilities.writeChartAsPNG(fos,chart,400,300);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
     */

}
