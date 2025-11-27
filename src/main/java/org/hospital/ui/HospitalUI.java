package org.hospital.ui;

import javax.swing.*;
import java.awt.*;
import org.hospital.ui.controller.*;
import org.hospital.ui.view.*;

/**
 * Main application window for Hospital Management System.
 * Uses MVC pattern with tabbed interface for different entities.
 */
public class HospitalUI extends JFrame {
    private JTabbedPane tabbedPane;
    
    // View panels
    private PacientePanel pacientePanel;
    private MedicoPanel medicoPanel;
    private SectorPanel sectorPanel;
    private HabitacionPanel habitacionPanel;
    private InternacionPanel internacionPanel;
    private GuardiaPanel guardiaPanel;
    private CamaDisponiblePanel camaDisponiblePanel;
    private VisitasMedicasPanel visitasMedicasPanel;
    private AuditoriaGuardiasPanel auditoriaGuardiasPanel;
    private VacacionesPanel vacacionesPanel;
    
    // Controllers - stored as fields to prevent garbage collection
    // Controllers manage listeners and must remain in scope
    // Suppressing "unused" warning as they ARE used (event listeners)
    @SuppressWarnings("unused")
    private PacienteController pacienteController;
    @SuppressWarnings("unused")
    private MedicoController medicoController;
    @SuppressWarnings("unused")
    private SectorController sectorController;
    @SuppressWarnings("unused")
    private HabitacionController habitacionController;
    @SuppressWarnings("unused")
    private InternacionController internacionController;
    @SuppressWarnings("unused")
    private GuardiaController guardiaController;
    @SuppressWarnings("unused")
    private CamaDisponibleController camaDisponibleController;
    @SuppressWarnings("unused")
    private VisitasMedicasController visitasMedicasController;
    @SuppressWarnings("unused")
    private AuditoriaGuardiasController auditoriaGuardiasController;
    @SuppressWarnings("unused")
    private VacacionesController vacacionesController;
    
    public HospitalUI() {
        initializeUI();
        initializeControllers();
    }
    
    private void initializeUI() {
        setTitle("Hospital Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleApplicationShutdown();
            }
        });
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Add header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create view panels
        pacientePanel = new PacientePanel();
        medicoPanel = new MedicoPanel();
        sectorPanel = new SectorPanel();
        habitacionPanel = new HabitacionPanel();
        internacionPanel = new InternacionPanel();
        guardiaPanel = new GuardiaPanel();
        camaDisponiblePanel = new CamaDisponiblePanel();
        visitasMedicasPanel = new VisitasMedicasPanel();
        auditoriaGuardiasPanel = new AuditoriaGuardiasPanel();
        vacacionesPanel = new VacacionesPanel();
        
        // Add tabs
        tabbedPane.addTab("Camas Disponibles", new ImageIcon(), camaDisponiblePanel, "View Available Beds Reports");
        tabbedPane.addTab("Visitas Médicas", new ImageIcon(), visitasMedicasPanel, "View Patient Internations & Visit Comments");
        tabbedPane.addTab("Auditoría Guardias", new ImageIcon(), auditoriaGuardiasPanel, "View Guard Assignment Audit Trail");
        tabbedPane.addTab("Guardias", new ImageIcon(), guardiaPanel, "Manage Guard Shifts");
        tabbedPane.addTab("Vacaciones", new ImageIcon(), vacacionesPanel, "Manage Doctor Vacations");
        tabbedPane.addTab("Pacientes", new ImageIcon(), pacientePanel, "Manage Patients");
        tabbedPane.addTab("Medicos", new ImageIcon(), medicoPanel, "Manage Doctors & Specialties");
        tabbedPane.addTab("Sectores", new ImageIcon(), sectorPanel, "Manage Sectors");
        tabbedPane.addTab("Habitaciones", new ImageIcon(), habitacionPanel, "Manage Rooms");
        tabbedPane.addTab("Internaciones", new ImageIcon(), internacionPanel, "Manage Hospitalizations");
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Add footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Hospital Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Bases de Datos | FI UNMdP - Grupo 4");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("© 2024 Hospital Database System | Grupo 4 - FI UNMdP");
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(127, 140, 141));
        
        panel.add(label);
        
        return panel;
    }
    
    private void initializeControllers() {
        // Initialize controllers for each view
        pacienteController = new PacienteController(pacientePanel);
        medicoController = new MedicoController(medicoPanel);
        sectorController = new SectorController(sectorPanel);
        habitacionController = new HabitacionController(habitacionPanel);
        internacionController = new InternacionController(internacionPanel);
        guardiaController = new GuardiaController(guardiaPanel);
        camaDisponibleController = new CamaDisponibleController(camaDisponiblePanel);
        visitasMedicasController = new VisitasMedicasController(visitasMedicasPanel);
        auditoriaGuardiasController = new AuditoriaGuardiasController(auditoriaGuardiasPanel);
        vacacionesController = new VacacionesController(vacacionesPanel);
    }
    
    /**
     * Handle application shutdown.
     * V1: No cleanup needed (basic JDBC, no connection pool)
     * V2 will add: Connection pool cleanup (HikariCP)
     */
    private void handleApplicationShutdown() {
        // No connection pool to close in V1 (basic JDBC)
        // V2 will add: DatabaseConfig.closeDataSource();
        dispose();
        System.exit(0);
    }
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show the UI
        SwingUtilities.invokeLater(() -> {
            HospitalUI app = new HospitalUI();
            app.setVisible(true);
        });
    }
}

