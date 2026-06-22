package sn.iage.isi.main;

import sn.iage.isi.entities.Book;
import sn.iage.isi.entities.Category;
import sn.iage.isi.repositories.BookRepository;
import sn.iage.isi.repositories.CategoryRepository;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // On initialise nos deux repositories
        CategoryRepository categoryRepository = new CategoryRepository();
        BookRepository bookRepository = new BookRepository();

        // 1. Récupération d'une catégorie existante (ou création d'une nouvelle si ta base est vide)
        List<Category> categories = categoryRepository.search("nou");
        Category categorieLivre;

        if (!categories.isEmpty()) {
            categorieLivre = categories.get(0);
        } else {
            // Si aucune catégorie n'est trouvée, on en crée une rapidement pour le test
            Category nouvelleCat = Category.builder()
                    .name("Nouvelle Catégorie")
                    .build();
            categorieLivre = categoryRepository.create(nouvelleCat);
        }

        // 2. Création du livre à tester

        Book book = Book.builder()
                .title("Mon premier livre JPA")
                .author("Auteur ISI")
                .publicationYear(2026)
                .countPages(250)
                .category(categorieLivre) // On le lie à la catégorie récupérée ou créée
                .build();

        // 3. Enregistrement du livre (ceci va générer l'ISBN automatiquement)
        System.out.println("Enregistrement du livre en cours...");
        Book livreEnregistre = bookRepository.createBook(book);

        System.out.println("Livre enregistré avec succès !");
        System.out.println("ID en base de données : " + livreEnregistre.getId());
        System.out.println("ISBN généré : " + livreEnregistre.getIsbn());

        // 4. Test de la méthode d'affichage global
        System.out.println("\n--- Liste de tous les livres en base ---");
        for (Book b : bookRepository.ListAllBooks()) {
            System.out.println("Titre : " + b.getTitle() + " | ISBN : " + b.getIsbn() + " | Catégorie : " + b.getCategory().getName());
        }

        // 5. Test du décompte total
        System.out.println("\nNombre total de livres : " + bookRepository.countAllBooks());
    }
}