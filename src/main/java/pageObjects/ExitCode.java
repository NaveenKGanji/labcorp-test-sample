package util.endToEnd.AdditionalUtilsEndtoEnd;

public class ExitCode {

    public String message;
    public int exitCode;
    public int number;
    public Boolean result;
    public String value;
    public double amt;
    public Object object;

    public ExitCode(){};

    public ExitCode(String message, int exitCode){
        this.message = message;
        this.exitCode = exitCode;
    }

    public ExitCode(String message, int number, int exitCode){
        this.message = message;
        this.number = number;
        this.exitCode = exitCode;
    }

    public ExitCode(String message, Boolean result){
        this.message = message;
        this.result = result;
    }

    public ExitCode(String message, Boolean result, double amt){
        this.message = message;
        this.result = result;
        this.amt = amt;
    }

    public ExitCode(String message, String value){
        this.message = message;
        this.value = value;
    }

    public ExitCode(String message, String value, int exitCode){
        this.message = message;
        this.value = value;
        this.exitCode = exitCode;
;
        }

    public ExitCode(String message, int exitCode, Object object){
        this.message = message;
        this.exitCode = exitCode;
        this.object = object;
    }

    public ExitCode(String message, String value, Object object){
        this.message = message;
        this.value = value;
        this.object = object;
    }

}
