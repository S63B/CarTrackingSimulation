package simulation;

public class Job<T> {

    private Runnable runnable;
    private T result;

    public Job(){
        this.runnable = runnable;
    }

    public Job(Runnable runnable){
        this.runnable = runnable;
    }

    public void setRunnable(Runnable runnable){
        this.runnable = runnable;
    }

    public void execute(){
        if (this.runnable == null){
            throw new IllegalStateException("Runnable is null; Call setRunnable() or Job(Runnable) first.");
        }

        runnable.run();
    }

    public T getResult(){
        return result;
    }

    public void setResult(T result){
        this.result = result;
    }

}
