package sn.iage.isi.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import sn.iage.isi.entities.Book;

import java.util.List;
import java.util.Random;

public class BookRepository {
    EntityManager em = JpaUtil.getEntityManager();

    public Book createBook(Book book) {
        EntityTransaction tx = em.getTransaction();

        // Génération de l'ISBN à la création
        book.setIsbn(generateIsbn());

        // Remplissage des champs de traçabilité obligatoires
        book.setUserCreated("admin");
        book.setUserUpdated("admin");

        try {
            tx.begin();
            em.persist(book);
            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        }
        return book;
    }

    public List<Book> ListAllBooks() {
        return em.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }

    public Book findBookById(int id) {
        Book book = em.find(Book.class, id);
        if(book == null)
            throw new EntityNotFoundException("Book not found");
        return book;
    }

    public Book findBookByIsbn(String isbn) {
        try {
            return em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Book updateBook(int id, Book newBook) {
        EntityTransaction tx = em.getTransaction();
        Book existingBook = findBookById(id);
        if(existingBook != null){
            existingBook.setTitle(newBook.getTitle());
            existingBook.setAuthor(newBook.getAuthor());
            existingBook.setPublicationYear(newBook.getPublicationYear());
            existingBook.setCategory(newBook.getCategory());
            existingBook.setUserUpdated("user");
            try{
                tx.begin();
                em.merge(existingBook);
                tx.commit();
            }catch(Exception e) {
                tx.rollback();
            }
        }
        return existingBook;
    }

    public void deleteBook(int id) {
        EntityTransaction tx = em.getTransaction();
        Book book = findBookById(id);
        try{
            tx.begin();
            em.remove(book);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    public List<Book> ListeBooksByCategory(String categoryName) {
        return em.createQuery("SELECT b FROM Book b WHERE LOWER(b.category.name) = :catName ORDER BY b.title", Book.class)
                .setParameter("catName", categoryName.toLowerCase())
                .getResultList();
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return em.createQuery("SELECT b FROM Book b WHERE LOWER(b.title) LIKE :kw ORDER BY b.title", Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        return em.createQuery("SELECT b FROM Book b WHERE LOWER(b.author) LIKE :kw ORDER BY b.title", Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public List<Book> searchBooksAfterYear(int year) {
        return em.createQuery("SELECT b FROM Book b WHERE b.publicationYear > :year ORDER BY b.publicationYear ASC", Book.class)
                .setParameter("year", year)
                .getResultList();
    }

    public List<Object[]> countBooksByCategory() {
        return em.createQuery("SELECT b.category.name, COUNT(b.id) FROM Book b GROUP BY b.category.name", Object[].class)
                .getResultList();
    }

    public int countAllBooks() {
        Long count = em.createQuery("SELECT COUNT(b.id) FROM Book b", Long.class)
                .getSingleResult();
        return count.intValue();
    }

    // ==========================================
    // MÉTHODES DE GÉNÉRATION D'ISBN FOURNIES
    // ==========================================

    private String generateIsbn() {
        String[] prefixes = {"978", "979"};
        Random random = new Random();

        String prefix = prefixes[random.nextInt(2)];
        String group = String.valueOf(random.nextInt(2));
        String publisher = String.format("%04d", random.nextInt(10000));
        String title    = String.format("%04d", random.nextInt(10000));

        String base = prefix + group + publisher + title;
        int checkDigit = computeIsbn13CheckDigit(base);

        return String.format("%s-%s-%s-%s-%d",
                prefix, group, publisher, title, checkDigit);
    }

    private int computeIsbn13CheckDigit(String base12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int remainder = sum % 10;
        return remainder == 0 ? 0 : 10 - remainder;
    }
}