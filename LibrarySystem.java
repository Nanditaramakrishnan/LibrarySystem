import java.util.*;

/**
 * Library Management System
 * A Java program demonstrating various OOP concepts and complexity patterns.
 */
public class LibrarySystem {

    // ─── Domain Model ────────────────────────────────────────────────────────

    static class Book {
        private String isbn;
        private String title;
        private String author;
        private String genre;
        private boolean checkedOut;
        private String borrowerName;
        private Date dueDate;

        public Book(String isbn, String title, String author, String genre) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.checkedOut = false;
        }

        public String getIsbn()        { return isbn; }
        public String getTitle()       { return title; }
        public String getAuthor()      { return author; }
        public String getGenre()       { return genre; }
        public boolean isCheckedOut()  { return checkedOut; }
        public String getBorrowerName(){ return borrowerName; }
        public Date getDueDate()       { return dueDate; }

        public void checkOut(String borrower, Date due) {
            this.checkedOut   = true;
            this.borrowerName = borrower;
            this.dueDate      = due;
        }

        public void returnBook() {
            this.checkedOut   = false;
            this.borrowerName = null;
            this.dueDate      = null;
        }

        @Override
        public String toString() {
            return String.format("[%s] \"%s\" by %s (%s) - %s",
                isbn, title, author, genre,
                checkedOut ? "Checked out to " + borrowerName : "Available");
        }
    }

    static class Member {
        private String memberId;
        private String name;
        private String email;
        private List<String> checkedOutIsbns;
        private int totalBorrowedCount;

        public Member(String memberId, String name, String email) {
            this.memberId         = memberId;
            this.name             = name;
            this.email            = email;
            this.checkedOutIsbns  = new ArrayList<>();
            this.totalBorrowedCount = 0;
        }

        public String getMemberId()             { return memberId; }
        public String getName()                 { return name; }
        public String getEmail()                { return email; }
        public List<String> getCheckedOutIsbns(){ return checkedOutIsbns; }
        public int getTotalBorrowedCount()      { return totalBorrowedCount; }

        public boolean canBorrow() {
            return checkedOutIsbns.size() < 5;
        }

        public void addBook(String isbn) {
            checkedOutIsbns.add(isbn);
            totalBorrowedCount++;
        }

        public void removeBook(String isbn) {
            checkedOutIsbns.remove(isbn);
        }

        @Override
        public String toString() {
            return String.format("Member[%s] %s <%s> — %d book(s) out",
                memberId, name, email, checkedOutIsbns.size());
        }
    }

    // ─── Catalog ─────────────────────────────────────────────────────────────

    static class Catalog {
        private Map<String, Book> books = new HashMap<>();

        public void addBook(Book book) {
            if (book == null || book.getIsbn() == null) {
                throw new IllegalArgumentException("Book or ISBN cannot be null");
            }
            if (books.containsKey(book.getIsbn())) {
                System.out.println("Book with ISBN " + book.getIsbn() + " already exists.");
                return;
            }
            books.put(book.getIsbn(), book);
        }

        public void removeBook(String isbn) {
            if (!books.containsKey(isbn)) {
                System.out.println("Book not found: " + isbn);
                return;
            }
            Book book = books.get(isbn);
            if (book.isCheckedOut()) {
                System.out.println("Cannot remove a checked-out book: " + isbn);
                return;
            }
            books.remove(isbn);
        }

        public Book findByIsbn(String isbn) {
            return books.get(isbn);
        }

        public List<Book> searchByTitle(String keyword) {
            List<Book> results = new ArrayList<>();
            for (Book b : books.values()) {
                if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                    results.add(b);
                }
            }
            return results;
        }

        public List<Book> searchByAuthor(String author) {
            List<Book> results = new ArrayList<>();
            for (Book b : books.values()) {
                if (b.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                    results.add(b);
                }
            }
            return results;
        }

        public List<Book> searchByGenre(String genre) {
            List<Book> results = new ArrayList<>();
            for (Book b : books.values()) {
                if (b.getGenre().equalsIgnoreCase(genre)) {
                    results.add(b);
                }
            }
            return results;
        }

        public List<Book> getAvailableBooks() {
            List<Book> available = new ArrayList<>();
            for (Book b : books.values()) {
                if (!b.isCheckedOut()) {
                    available.add(b);
                }
            }
            return available;
        }

        public List<Book> getAllBooks() {
            return new ArrayList<>(books.values());
        }
    }

    // ─── Member Registry ─────────────────────────────────────────────────────

    static class MemberRegistry {
        private Map<String, Member> members = new HashMap<>();
        private int nextId = 1000;

        public Member registerMember(String name, String email) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email address");
            }
            for (Member m : members.values()) {
                if (m.getEmail().equalsIgnoreCase(email)) {
                    System.out.println("Member with email " + email + " already registered.");
                    return m;
                }
            }
            String id = "M" + (nextId++);
            Member member = new Member(id, name, email);
            members.put(id, member);
            return member;
        }

        public Member findById(String memberId) {
            return members.get(memberId);
        }

        public List<Member> getAllMembers() {
            return new ArrayList<>(members.values());
        }

        public void removeMember(String memberId) {
            Member m = members.get(memberId);
            if (m == null) {
                System.out.println("Member not found: " + memberId);
                return;
            }
            if (!m.getCheckedOutIsbns().isEmpty()) {
                System.out.println("Cannot remove member with outstanding books: " + memberId);
                return;
            }
            members.remove(memberId);
        }
    }

    // ─── Loan Service ─────────────────────────────────────────────────────────

    static class LoanService {
        private Catalog catalog;
        private MemberRegistry registry;
        private static final int LOAN_DAYS = 14;

        public LoanService(Catalog catalog, MemberRegistry registry) {
            this.catalog  = catalog;
            this.registry = registry;
        }

        public boolean checkOut(String memberId, String isbn) {
            Member member = registry.findById(memberId);
            if (member == null) {
                System.out.println("Member not found: " + memberId);
                return false;
            }

            Book book = catalog.findByIsbn(isbn);
            if (book == null) {
                System.out.println("Book not found: " + isbn);
                return false;
            }

            if (book.isCheckedOut()) {
                System.out.println("Book is already checked out: " + isbn);
                return false;
            }

            if (!member.canBorrow()) {
                System.out.println("Member has reached the borrowing limit: " + memberId);
                return false;
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, LOAN_DAYS);
            Date dueDate = cal.getTime();

            book.checkOut(member.getName(), dueDate);
            member.addBook(isbn);
            System.out.println("Checked out \"" + book.getTitle() + "\" to " + member.getName()
                + ". Due: " + dueDate);
            return true;
        }

        public boolean returnBook(String memberId, String isbn) {
            Member member = registry.findById(memberId);
            if (member == null) {
                System.out.println("Member not found: " + memberId);
                return false;
            }

            Book book = catalog.findByIsbn(isbn);
            if (book == null) {
                System.out.println("Book not found: " + isbn);
                return false;
            }

            if (!book.isCheckedOut()) {
                System.out.println("Book is not currently checked out: " + isbn);
                return false;
            }

            if (!member.getCheckedOutIsbns().contains(isbn)) {
                System.out.println("This book was not borrowed by member: " + memberId);
                return false;
            }

            book.returnBook();
            member.removeBook(isbn);
            System.out.println("Returned \"" + book.getTitle() + "\" from " + member.getName());
            return true;
        }

        public void printOverdueBooks() {
            Date today = new Date();
            System.out.println("\n--- Overdue Books ---");
            boolean found = false;
            for (Book b : catalog.getAllBooks()) {
                if (b.isCheckedOut() && b.getDueDate() != null && b.getDueDate().before(today)) {
                    System.out.println(b.getTitle() + " | Borrower: " + b.getBorrowerName()
                        + " | Due: " + b.getDueDate());
                    found = true;
                }
            }
            if (!found) System.out.println("No overdue books.");
        }
    }

    // ─── Report Generator ─────────────────────────────────────────────────────

    static class ReportGenerator {
        private Catalog catalog;
        private MemberRegistry registry;

        public ReportGenerator(Catalog catalog, MemberRegistry registry) {
            this.catalog  = catalog;
            this.registry = registry;
        }

        public void printCatalogSummary() {
            List<Book> all       = catalog.getAllBooks();
            List<Book> available = catalog.getAvailableBooks();
            System.out.println("\n=== Catalog Summary ===");
            System.out.println("Total books    : " + all.size());
            System.out.println("Available      : " + available.size());
            System.out.println("Checked out    : " + (all.size() - available.size()));
        }

        public void printGenreBreakdown() {
            Map<String, Integer> counts = new HashMap<>();
            for (Book b : catalog.getAllBooks()) {
                counts.merge(b.getGenre(), 1, Integer::sum);
            }
            System.out.println("\n=== Books by Genre ===");
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue());
            }
        }

        public void printTopBorrowers(int n) {
            List<Member> members = registry.getAllMembers();
            members.sort((a, b) -> b.getTotalBorrowedCount() - a.getTotalBorrowedCount());
            System.out.println("\n=== Top " + n + " Borrowers ===");
            for (int i = 0; i < Math.min(n, members.size()); i++) {
                Member m = members.get(i);
                System.out.println((i + 1) + ". " + m.getName()
                    + " — " + m.getTotalBorrowedCount() + " total borrowed");
            }
        }

        public void printMemberReport(String memberId) {
            Member m = registry.findById(memberId);
            if (m == null) {
                System.out.println("Member not found: " + memberId);
                return;
            }
            System.out.println("\n=== Member Report: " + m.getName() + " ===");
            System.out.println("ID      : " + m.getMemberId());
            System.out.println("Email   : " + m.getEmail());
            System.out.println("Currently borrowed: " + m.getCheckedOutIsbns().size());
            System.out.println("Total ever borrowed: " + m.getTotalBorrowedCount());
            if (!m.getCheckedOutIsbns().isEmpty()) {
                System.out.println("Current books:");
                for (String isbn : m.getCheckedOutIsbns()) {
                    Book b = catalog.findByIsbn(isbn);
                    if (b != null) System.out.println("  - " + b.getTitle());
                }
            }
        }
    }

    // ─── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Catalog catalog          = new Catalog();
        MemberRegistry registry  = new MemberRegistry();
        LoanService loans        = new LoanService(catalog, registry);
        ReportGenerator reporter = new ReportGenerator(catalog, registry);

        // Seed catalog
        catalog.addBook(new Book("978-0-13-110362-7", "The C Programming Language",  "Kernighan & Ritchie", "Programming"));
        catalog.addBook(new Book("978-0-13-468599-1", "The Pragmatic Programmer",    "Hunt & Thomas",       "Programming"));
        catalog.addBook(new Book("978-0-13-235088-4", "Clean Code",                  "Robert C. Martin",    "Programming"));
        catalog.addBook(new Book("978-0-596-51774-8", "JavaScript: The Good Parts", "Douglas Crockford",   "Programming"));
        catalog.addBook(new Book("978-0-7432-7356-5", "The Great Gatsby",            "F. Scott Fitzgerald", "Fiction"));
        catalog.addBook(new Book("978-0-06-112008-4", "To Kill a Mockingbird",       "Harper Lee",          "Fiction"));
        catalog.addBook(new Book("978-0-14-028329-7", "1984",                        "George Orwell",       "Fiction"));
        catalog.addBook(new Book("978-0-316-76948-0", "The Catcher in the Rye",      "J.D. Salinger",       "Fiction"));

        // Register members
        Member alice = registry.registerMember("Alice Johnson", "alice@example.com");
        Member bob   = registry.registerMember("Bob Smith",     "bob@example.com");
        Member carol = registry.registerMember("Carol White",   "carol@example.com");

        // Perform loans
        loans.checkOut(alice.getMemberId(), "978-0-13-235088-4");
        loans.checkOut(alice.getMemberId(), "978-0-14-028329-7");
        loans.checkOut(bob.getMemberId(),   "978-0-13-110362-7");
        loans.checkOut(carol.getMemberId(), "978-0-7432-7356-5");

        // Return one book
        loans.returnBook(alice.getMemberId(), "978-0-14-028329-7");

        // Reports
        reporter.printCatalogSummary();
        reporter.printGenreBreakdown();
        reporter.printTopBorrowers(3);
        reporter.printMemberReport(alice.getMemberId());
        loans.printOverdueBooks();

        // Search demo
        System.out.println("\n=== Search: 'clean' ===");
        for (Book b : catalog.searchByTitle("clean")) System.out.println(b);

        System.out.println("\n=== Available Books ===");
        for (Book b : catalog.getAvailableBooks()) System.out.println(b);
    }
}
