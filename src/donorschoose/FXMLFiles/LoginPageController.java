package donorschoose.FXMLFiles;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class LoginPageController implements Initializable {
    
    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;   


    @FXML
    private void login()
    {
        
    }

    @FXML
    private void loadRegistrationPage() throws Exception
    {
        Parent registrationPage = FXMLLoader.load(getClass().getResource("registrationPage.fxml"));
        Scene scene = new Scene(registrationPage);
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(scene);
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    } 
    
}