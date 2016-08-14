
import entidades.ImageTransform;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kcrez
 */
public final class PPD_Frame extends javax.swing.JFrame implements SerialPortEventListener {

    /**
     * Creates new form LB_Frame
     */
    
    private static final String TURN_ON_RED = "0";
    private static final String TURN_OFF_RED = "1";
    private static final String TURN_ON_GREEN = "2";
    private static final String TURN_OFF_GREEN = "3";
    private static final String TURN_ON_BLUE = "4";
    private static final String TURN_OFF_BLUE = "5";
    private static final String TURN_ON_WHITE = "6";
    private static final String TURN_OFF_WHITE = "7";
    
    /*
     * Variables para la conexión
     */
    private OutputStream output;
    private BufferedReader input;
    SerialPort serial;
    /*
     * Ejecutar la siguiente instrucción en la terminal si estas utilizando Ubuntu
     * ln -s /dev/ttyACM0 /dev/ttyS0
     * Si no, cambiar el puerto al que se ha conectado el arduino
     */
    private final String PORT[] = { "/dev/ttyACM0", "/dev/ttyS0", "COM1","COM2", "COM3", "COM4", "COM5" }; //puerto de conexion del arduino
    private final int TIMEOUT = 2000; //milisegundos
    private final int DATA_RATE = 9600;
    int Bandera = 0;

    public PPD_Frame() {
        initComponents();
        inicializarConexion();
        setTitle("Prueba PlayDuino");
        //setIconImage(new ImageIcon(getClass().getResource("src/img/logo.png")).getImage());
        //jRadioButton3.setSelected(true);
    }

    public void inicializarConexion() {
        CommPortIdentifier puertoID = null;
        Enumeration puertoEnum = CommPortIdentifier.getPortIdentifiers();
        
        while(puertoEnum.hasMoreElements()) {
            CommPortIdentifier actualPuertoID = (CommPortIdentifier) puertoEnum.nextElement();
            for(String portName : PORT) {
                if(actualPuertoID.getName().equals(portName)) {
                    puertoID = actualPuertoID;
                    break;
                }
            }
        }

        if(puertoID == null) {
            mostrarError("No se puede conectar al puerto");
            System.exit(ERROR);
        }

        try{
            serial = (SerialPort) puertoID.open(this.getClass().getName(), TIMEOUT);
            //parametros puerto serial
            serial.setSerialPortParams(DATA_RATE, serial.DATABITS_8, serial.STOPBITS_1, serial.PARITY_NONE);
            
            output = serial.getOutputStream();
            input = new BufferedReader(new InputStreamReader(serial.getInputStream()));
            
            serial.addEventListener(this);
            serial.notifyOnDataAvailable(true);

        } catch(Exception e) {
            mostrarError("Error obteniendo comunicación con el arduino");
            System.exit(ERROR);
        }
    }
    
    /**
      * Esta función se utiliza cuandod dejamos de utilizar el puerto serial.
      * Esto permite que el puerto no se bloquee en plataformas Linux.
      */
    public synchronized void close() {
        if (serial != null) {
            serial.removeEventListener();
            serial.close();
        }
    }
    
    /*private void RecibirDatos() throws IOException {
        int Output = input.read();
        String salida = String.valueOf(Output);
        jLabel6.setText("Adios");
    }*/
    
    private void enviarDatos(String datos) {
        try{
            output.write(datos.getBytes());
        } catch(IOException e) {
            mostrarError(e.getMessage());
            System.exit(ERROR);
        }
    }
    
    private void numerosSegmentos(String valor) {
        switch(valor.length()) {
            case 3:
                jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(2) + ".png")));
                jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(1) + ".png")));
                jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(0) + ".png")));
                break;
            case 2:
                jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(1) + ".png")));
                jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(0) + ".png")));
                jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png")));
                break;
            case 1:
                jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/" + valor.charAt(0) + ".png")));
                jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png")));
                jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png")));
                break;
        }
    }
    
    private void velocimetro(String s) throws IOException {
        BufferedImage img = loadJPGImage("src/img/aguja.png");
        BufferedImage dst = rotacionImagen(img, 30);
        saveJPGImage(dst);
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/aguja.png")));
    }
    
    private static void saveJPGImage(BufferedImage im) throws IOException {
        ImageIO.write(im, "PNG", new File("src/img/aguja.png"));
    }
    
    public void mostrarError(String msj) {
        JOptionPane.showMessageDialog(this, msj, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
    
    public static BufferedImage rotacionImagen(BufferedImage origen, double grados) {
        BufferedImage destinationImage;
        ImageTransform imTransform = new ImageTransform(origen.getHeight(), origen.getWidth());
        imTransform.rotate(grados);
        imTransform.findTranslation();
        AffineTransformOp ato = new AffineTransformOp(imTransform.getTransform(), AffineTransformOp.TYPE_BILINEAR);
        destinationImage = ato.createCompatibleDestImage(origen, origen.getColorModel());
        return ato.filter(origen, destinationImage);
    }

    private static BufferedImage loadJPGImage(String ruta) throws IOException {
        BufferedImage imagen = ImageIO.read(new File(ruta));

        BufferedImage source = new BufferedImage(imagen.getWidth(),
                imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        source.getGraphics().drawImage(imagen, 0, 0, null);
        return source;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton5 = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(676, 497));
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Leds", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(229, 83, 83)));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/greenoff.png"))); // NOI18N
        jLabel2.setText("jLabel2");
        jLabel2.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel2.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel2.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/whiteoff.png"))); // NOI18N
        jLabel4.setText("jLabel4");
        jLabel4.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel4.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel4.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/blueoff.png"))); // NOI18N
        jLabel3.setText("jLabel3");
        jLabel3.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel3.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel3.setPreferredSize(new java.awt.Dimension(50, 50));

        jRadioButton5.setText("Led Blanco");

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logo.png"))); // NOI18N
        jLabel5.setText("jLabel5");
        jLabel5.setMaximumSize(new java.awt.Dimension(105, 97));
        jLabel5.setMinimumSize(new java.awt.Dimension(105, 97));
        jLabel5.setPreferredSize(new java.awt.Dimension(105, 97));

        jRadioButton3.setText("Led Rojo");

        jRadioButton4.setText("Led Azul");

        jButton1.setFont(new java.awt.Font("Padauk", 1, 15)); // NOI18N
        jButton1.setText("ENCENDER");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Padauk", 1, 15)); // NOI18N
        jButton2.setText("APAGAR");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Led Verde");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/redoff.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jLabel1.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel1.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jRadioButton5)
                            .addComponent(jRadioButton3)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(123, 123, 123))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Potenciometros", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(229, 83, 83)));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pot. A3", javax.swing.border.TitledBorder.RIGHT, javax.swing.border.TitledBorder.TOP, null, new java.awt.Color(85, 152, 17)));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png"))); // NOI18N

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png"))); // NOI18N

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/num/0.png"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pot. A4", javax.swing.border.TitledBorder.RIGHT, javax.swing.border.TitledBorder.TOP, null, new java.awt.Color(85, 152, 17)));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 124, Short.MAX_VALUE)
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pot. A5", javax.swing.border.TitledBorder.RIGHT, javax.swing.border.TitledBorder.TOP, null, new java.awt.Color(85, 152, 17)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/aguja.png"))); // NOI18N
        jLabel12.setToolTipText("");
        jLabel12.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel12.setDebugGraphicsOptions(javax.swing.DebugGraphics.BUFFERED_OPTION);
        jLabel12.setDoubleBuffered(true);
        jLabel12.setFocusable(false);
        jLabel12.setMaximumSize(new java.awt.Dimension(100, 30));
        jLabel12.setMinimumSize(new java.awt.Dimension(100, 30));
        jPanel6.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 100, -1));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lbpot5.png"))); // NOI18N
        jLabel10.setText("jLabel10");
        jLabel10.setMaximumSize(new java.awt.Dimension(280, 142));
        jLabel10.setMinimumSize(new java.awt.Dimension(280, 142));
        jLabel10.setPreferredSize(new java.awt.Dimension(280, 142));
        jPanel6.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 17, 280, -1));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Botones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(229, 83, 83)));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/btoff.png"))); // NOI18N
        jLabel6.setText("jLabel6");
        jLabel6.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel6.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel6.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/btoff.png"))); // NOI18N
        jLabel7.setText("jLabel7");
        jLabel7.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel7.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel7.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/btoff.png"))); // NOI18N
        jLabel8.setText("jLabel8");
        jLabel8.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel8.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel8.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/btoff.png"))); // NOI18N
        jLabel9.setText("jLabel9");
        jLabel9.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel9.setMinimumSize(new java.awt.Dimension(50, 50));
        jLabel9.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(jRadioButton3.isSelected()) {
            enviarDatos(TURN_ON_RED);
            jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/redon.png")));
        }
        if(jRadioButton2.isSelected()) {
            enviarDatos(TURN_ON_GREEN);
            jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/greenon.png")));
        }
        if(jRadioButton4.isSelected()) {
            enviarDatos(TURN_ON_BLUE);
            jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/blueon.png")));
        }
        if(jRadioButton5.isSelected()) {
            enviarDatos(TURN_ON_WHITE);
            jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/whiteon.png")));
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        if(jRadioButton3.isSelected()) {
            enviarDatos(TURN_OFF_RED);
            jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/redoff.png")));
        }
        if(jRadioButton2.isSelected()) {
            enviarDatos(TURN_OFF_GREEN);
            jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/greenoff.png")));
        }
        if(jRadioButton4.isSelected()) {
            enviarDatos(TURN_OFF_BLUE);
            jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/blueoff.png")));
        }
        if(jRadioButton5.isSelected()) {
            enviarDatos(TURN_OFF_WHITE);
            jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/whiteoff.png")));
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PPD_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PPD_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PPD_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PPD_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PPD_Frame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    // End of variables declaration//GEN-END:variables

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine;
                if (input.ready()) {
                    inputLine = input.readLine();
                    String arreInLine[] = inputLine.split(",");
                    for(String valor : arreInLine){
                        switch(Bandera) {
                            case 0:
                                numerosSegmentos(valor);
                                break;
                            case 1:
                                System.out.println(valor);
                                break;
                            case 2:
                                velocimetro(valor);
                                break;
                            case 3:
                                jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/bt" + valor + ".png")));
                                break;
                            case 4:
                                jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/bt" + valor + ".png")));
                                break;
                            case 5:
                                jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/bt" + valor + ".png")));
                                break;
                            case 6:
                                jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/bt" + valor + ".png")));
                                break;
                        }
                        Bandera++;
                    }
                    Bandera = 0;
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }
    
    /*
    https://tecdigital.tec.ac.cr/revistamatematica/HERRAmInternet/Graficador-Swing-java2D/node3.html
    */
    /*void Graficar(Graphics ap, int xg, int yg)
    {
        int xi=0,yi=0,xi1=0,yi1=0,numPuntos=1;
        int cxmin,cxmax,cymin,cymax;
        double valxi=0.0, valxi1=0.0, valyi=0.0,valyi1=0.0;
        
        Complex valC; //manejo de complejos en JEP
        double imgx;

        //convertimos el objeto ap en un objeto Graphics2D para usar los métodos Java2D
        Graphics2D g = (Graphics2D) ap;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(ft10);
        g.setPaint(new Color(0,0,150));

        //eje Y
        g.draw(new Line2D.Double(xg, 10, xg, Galto-10));
        //eje X
        g.draw(new Line2D.Double(10, yg, Gancho-10, yg));
        
        xmin = -1.0*xg/escalaX;
        xmax = (1.0*(Gancho-xg)/escalaX);
        cxmin = (int)Math.round(xmin); //pantalla
        cxmax = (int)Math.round(xmax);
        cymin = (int)Math.round(1.0*(yg-Galto)/escalaY);
        cymax = (int)Math.round(1.0*yg/escalaY);
        
        numPuntos=Gancho; //num pixels
        g.setPaint(Color.gray);
        g.setFont(ft7);

        //marcas en los ejes (ticks)
        if(escalaX>5) {
            for(int i=cxmin+1;i<cxmax;i++) {
                g.draw(new Line2D.Double(xg+i*escalaX, yg-2, xg+i*escalaX , yg+2));
                if(i>0)
                    g.drawString(""+i, xg+i*escalaX-2, yg+12);
                if(i<0)
                    g.drawString(""+i, xg+i*escalaX-6, yg+12);
            }
        }
        if(escalaY>5) {
            for(int i=cymin+1;i<cymax;i++) {
                g.draw(new Line2D.Double(xg-2, yg-i*escalaY, xg+2 , yg-i*escalaY));
                if(i>0)
                    g.drawString(""+i, xg-12,yg-i*escalaY+3 );
                if(i<0)
                    g.drawString(""+i, xg-14,yg-i*escalaY+3 );
            }
        }
        g.setPaint(new Color(50,100,0));
        g.setStroke(grosor1);
        miEvaluador.parseExpression(Tffun.getText());
        errorEnExpresion = miEvaluador.hasError(); //hay error?
        
        if(!errorEnExpresion) {
            Tffun.setForeground(Color.black);

    for(int i=0;i<numPuntos-1;i++)
    {
      valxi   =xmin +i*1.0/escalaX;
      valxi1  =xmin+(i+1)*1.0/escalaX;
      miEvaluador.addVariable("x", valxi);
      valyi   = miEvaluador.getValue();
      miEvaluador.addVariable("x", valxi1);
      valyi1  = miEvaluador.getValue();
      xi      =(int)Math.round(escalaX*(valxi));
      yi      =(int)Math.round(escalaY*valyi);
      xi1     =(int)Math.round(escalaX*(valxi1));
      yi1     =(int)Math.round(escalaY*valyi1);

     //control de discontinuidades groseras y complejos
      valC = miEvaluador.getComplexValue();
      imgx = (double)Math.abs(valC.im());
     if(dist(valxi,valyi,valxi1,valyi1)< 1000 && imgx==0)
     {
       g.draw(new Line2D.Double(xg+xi,yg-yi,xg+xi1,yg-yi1));
     } 
    }//fin del for
  }else{mensaje.setText(":. Hay un error.");
        Tffun.setForeground(Color.red);
 }
}//*/
}