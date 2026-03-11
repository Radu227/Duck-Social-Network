package repository;
import java.util.List;

public class Page<E> {
    private List<E> elementsOnPage;
    private int totalElementCount;

    public Page(List<E> elementsOnPage, int totalElementCount) {
        this.elementsOnPage = elementsOnPage;
        this.totalElementCount = totalElementCount;
    }
    public List<E> getElementsOnPage() { return elementsOnPage; }
    public int getTotalElementCount() { return totalElementCount; }
}