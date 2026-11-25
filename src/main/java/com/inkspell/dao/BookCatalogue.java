package com.inkspell.dao;

import com.inkspell.model.BookItem;
import com.inkspell.model.ReviewItem;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hardcoded book catalogue data provider for InkSpell.
 * Contains 108 unique, real published books across 12 distinct genres
 * (9 books per genre). Provides fast, in-memory searching, filtering,
 * and sorting, as well as a safe synchronization mechanism to MySQL.
 */
public final class BookCatalogue {

  public static final List<String> GENRES =
      List.of(
          "Fantasy",
          "Mystery",
          "Thriller",
          "Horror",
          "Romance",
          "Dark Romance",
          "Science Fiction",
          "Contemporary Fiction",
          "Historical Fiction",
          "Young Adult",
          "Gothic Fiction",
          "Psychological Thriller");

  private static final List<BookItem> ALL_BOOKS = new java.util.concurrent.CopyOnWriteArrayList<>();
  private static final Map<Long, BookItem> BOOKS_BY_ID = new java.util.concurrent.ConcurrentHashMap<>();

  static {
    List<BookItem> list = new ArrayList<>(108);

    // =========================================================================
    // 1. FANTASY (IDs 1 - 9)
    // =========================================================================
    list.add(
        book(
            1L,
            "The Hobbit",
            "J. R. R. Tolkien",
            "Fantasy",
            "Bilbo Baggins enjoys a comfortable, quiet life in the Shire until the wizard Gandalf and a company of thirteen dwarves arrive at his door. Swept into a perilous quest to reclaim the lonely mountain and its treasure from the dragon Smaug, Bilbo discovers courage, resourcefulness, and a peculiar golden ring that will alter history.",
            "9780547928227",
            4.8,
            540,
            1937,
            310,
            "English",
            List.of("Classic High Fantasy", "Quest", "Middle-earth", "Dragon"),
            true,
            98,
            review(
                "Gideon Croft",
                "September 12, 2025",
                5,
                "A timeless journey filled with warmth, peril, and the quiet courage of small folk."),
            review(
                "Rowan Blackwood",
                "October 4, 2025",
                5,
                "Tolkien's worldbuilding feels like stepping into an ancient living legend."),
            review(
                "Ariadne Vance",
                "November 1, 2025",
                4,
                "The riddles in the dark chapter remains an absolute masterclass in suspense.")));

    list.add(
        book(
            2L,
            "The Fellowship of the Ring",
            "J. R. R. Tolkien",
            "Fantasy",
            "In ancient Middle-earth, young Frodo Baggins is entrusted with the One Ring, an artifact of immense dark power forged by the Dark Lord Sauron. Accompanied by a loyal band of hobbits, men, an elf, a dwarf, and the wizard Gandalf, Frodo sets forth toward Mordor on an arduous expedition where friendship proves mightier than fear.",
            "9780547928210",
            4.9,
            610,
            1954,
            423,
            "English",
            List.of("Epic Fantasy", "One Ring", "Fellowship", "Mythic Lore"),
            false,
            99,
            review(
                "Julian Sterling",
                "August 19, 2025",
                5,
                "The standard by which every high fantasy epic is measured. Immense and emotional."),
            review(
                "Seraphina Moon",
                "September 28, 2025",
                5,
                "Lothlórien and the mines of Moria linger in the imagination for decades."),
            review(
                "Cedric Hawke",
                "October 15, 2025",
                5,
                "The bond between the companions gives real heart to this monumental myth.")));

    list.add(
        book(
            3L,
            "A Wizard of Earthsea",
            "Ursula K. Le Guin",
            "Fantasy",
            "On the windswept island of Gont, a reckless young prodigy named Ged discovers his innate talent for magic and enters the great school at Roke. In a moment of foolish arrogance, Ged accidentally looses an ancient shadow beast into the archipelago, beginning a lifelong trial of humility, balance, and the sanctity of true names.",
            "9780547773742",
            4.6,
            320,
            1968,
            183,
            "English",
            List.of("Magic School", "True Names", "Archipelago", "Coming of Age"),
            false,
            88,
            review(
                "Elise Thorne",
                "July 22, 2025",
                5,
                "Poetic, philosophical, and deeply original. Le Guin explores shadow and self brilliantly."),
            review(
                "Bramwell Oakes",
                "September 5, 2025",
                4,
                "A refreshing, meditative antidote to bombastic combat fantasy."),
            review(
                "Kaelen Drake",
                "October 29, 2025",
                5,
                "The concept of magic rooted in naming and balance is truly unforgettable.")));

    list.add(
        book(
            4L,
            "The Name of the Wind",
            "Patrick Rothfuss",
            "Fantasy",
            "Inside the quiet Waystone Inn, the tavern keeper Kote reveals his true identity as Kvothe, the legendary arcanist, musician, and fugitive whispered across four corners of the civilization. He recounts his impoverished beginnings in a traveling troupe, his desperate admission to the mysterious University, and his relentless hunt for the mythical Chandrian.",
            "9780756404741",
            4.7,
            490,
            2007,
            662,
            "English",
            List.of("Hero's Journey", "University", "Sympathy Magic", "Storytelling"),
            false,
            94,
            review(
                "Valerie Marsh",
                "June 14, 2025",
                5,
                "The prose flows like music. Kvothe's struggles at the University are mesmerizing."),
            review(
                "Theron Grey",
                "August 11, 2025",
                4,
                "Brilliant magic system and rich storytelling that demands multiple rereads."),
            review(
                "Faye Lindisfarne",
                "October 18, 2025",
                5,
                "A storytelling masterpiece where words carry visceral weight and wonder.")));

    list.add(
        book(
            5L,
            "The Way of Kings",
            "Brandon Sanderson",
            "Fantasy",
            "On the tempest-wracked continent of Roshar, monumental highstorms unleash magical energy across a stone landscape where ancient armor grants godlike strength. As four unlikely champions—a disgraced surgeon turned slave, an assassin who weeps as he kills, an earnest young scholar, and a conflicted highprince—cross paths, the forgotten Radiants must awaken.",
            "9780765326355",
            4.8,
            520,
            2010,
            1007,
            "English",
            List.of("High Fantasy", "Stormlight", "Shardblades", "Worldbuilding"),
            false,
            96,
            review(
                "Dorian Vale",
                "May 30, 2025",
                5,
                "Kaladin's journey in Bridge Four is one of the most inspiring arcs in modern fantasy."),
            review(
                "Miriam S.",
                "September 19, 2025",
                5,
                "Unmatched scale and magic mechanics. The Sanderstorm ending left me breathless."),
            review(
                "Eldridge Cole",
                "November 10, 2025",
                4,
                "Massive world, intricate lore, and payoff that rewards every page invested.")));

    list.add(
        book(
            6L,
            "The Priory of the Orange Tree",
            "Samantha Shannon",
            "Fantasy",
            "An unmarried queen must conceive an heir to protect her queendom from ancient decay, while assassins close in on her palace chambers. Across the seas, a secret order of mages practices forbidden dragon magic, and an outsider dragon rider trains in eastern skies, all as the Nameless One stirs from a thousand-year slumber.",
            "9781635570298",
            4.5,
            310,
            2019,
            848,
            "English",
            List.of("Dragons", "Court Politics", "Dual Kingdoms", "Queer Fantasy"),
            false,
            87,
            review(
                "Nadia Rivera",
                "August 3, 2025",
                5,
                "Expansive, feminist fantasy with majestic dragons and intricate court diplomacy."),
            review(
                "Harlan Brooks",
                "September 14, 2025",
                4,
                "Rich mythology and compelling point-of-view characters across opposing shores."),
            review(
                "Cassandra Fox",
                "October 22, 2025",
                5,
                "Ead Duryan and Queen Sabran share an unforgettable, quiet dynamic.")));

    list.add(
        book(
            7L,
            "The Night Circus",
            "Erin Morgenstern",
            "Fantasy",
            "Le Cirque des Rêves arrives unannounced at dusk, a monochrome labyrinth of black-and-white striped tents filled with breathtaking wonders that appear only by starlight. Within its velvet corridors, two young illusionists, Celia and Marco, are bound by ancient masters to an enchanting duel of enchantments, unaware that only one competitor can survive the stage.",
            "9780307744432",
            4.6,
            430,
            2011,
            387,
            "English",
            List.of("Magical Realism", "Victorian Illusion", "Star-Crossed", "Circus"),
            true,
            91,
            review(
                "Lydia Winter",
                "July 8, 2025",
                5,
                "Atmospheric perfection. It feels like breathing in starlight, caramel, and old parchment."),
            review(
                "Silas Vane",
                "August 27, 2025",
                5,
                "A lush sensory dream that makes you wish the circus would appear in your town."),
            review(
                "Clara Beauchamp",
                "October 12, 2025",
                4,
                "The imaginative clock and ice garden tents are peak fantasy craftsmanship.")));

    list.add(
        book(
            8L,
            "Piranesi",
            "Susanna Clarke",
            "Fantasy",
            "Piranesi lives in the House, an infinite labyrinth of majestic halls lined with thousands of classical statues, where ocean tides crash through lower staircases and clouds drift through upper colonnades. Trusting in the benevolence of this ocean-filled sanctuary, Piranesi meticulously catalogues the tides until clues hint at another visitor and a forgotten world beyond the marble walls.",
            "9781635575637",
            4.7,
            380,
            2020,
            245,
            "English",
            List.of("Mystical Labyrinth", "Tides & Statues", "Solitude", "Philosophical"),
            false,
            90,
            review(
                "Arthur Pendelton",
                "September 1, 2025",
                5,
                "A gentle, luminous, and haunting puzzle. Piranesi's innocence is profoundly moving."),
            review(
                "Elena Rostova",
                "October 8, 2025",
                5,
                "The marble halls and crashing tides stayed in my thoughts long after finishing."),
            review(
                "Gwen Marlowe",
                "November 2, 2025",
                4,
                "A quiet masterpiece of wonder, mystery, and the sanctity of forgotten spaces.")));

    list.add(
        book(
            9L,
            "A Game of Thrones",
            "George R. R. Martin",
            "Fantasy",
            "In a realm where summers span decades and harsh winters can last a lifetime, Lord Eddard Stark leaves his northern fortress to become Hand to King Robert Baratheon. Caught between scheming dynasties, a treacherous queen, and whispers of resurrected terrors gathering beyond the Great Wall of ice, the kingdom hurtles toward war.",
            "9780553593716",
            4.8,
            580,
            1996,
            694,
            "English",
            List.of("Political Intrigue", "Westeros", "Dynasties", "Winter Is Coming"),
            false,
            97,
            review(
                "Cormac Bell",
                "June 20, 2025",
                5,
                "Unforgiving and brilliant political intrigue. Every character is morally layered."),
            review(
                "Theresa Locke",
                "August 15, 2025",
                5,
                "The pacing never falters. The realism and high stakes changed fantasy forever."),
            review(
                "Finnian Drake",
                "October 31, 2025",
                4,
                "Ned Stark's honor against the ruthless Lannister court remains unforgettable.")));

    // =========================================================================
    // 2. MYSTERY (IDs 10 - 18)
    // =========================================================================
    list.add(
        book(
            10L,
            "The Murder of Roger Ackroyd",
            "Agatha Christie",
            "Mystery",
            "In the sleepy English hamlet of King's Abbot, wealthy widower Roger Ackroyd is found stabbed through the neck inside his study shortly after receiving a letter containing a dead woman's confession. The retired Belgian sleuth Hercule Poirot is called out of quiet gardening to unravel a puzzle where every resident conceals a secret, leading to one of the most stunning climaxes in detective fiction.",
            "9780062073563",
            4.8,
            460,
            1926,
            288,
            "English",
            List.of(
                "Classic Whodunit", "Hercule Poirot", "English Village", "Unreliable Narrator"),
            true,
            95,
            review(
                "Inspector Croft",
                "September 10, 2025",
                5,
                "The ending redefined detective fiction. Christie's structural brilliance is unmatched."),
            review(
                "Martha Gable",
                "October 16, 2025",
                5,
                "Poirot is at his most methodical, dissecting secrets with quiet charm."),
            review(
                "Jonathan Pierce",
                "November 5, 2025",
                4,
                "A legendary twist that plays totally fair with the observant reader.")));

    list.add(
        book(
            11L,
            "The Hound of the Baskervilles",
            "Arthur Conan Doyle",
            "Mystery",
            "When Sir Charles Baskerville dies under mysterious circumstances upon the desolate Dartmoor moor, ghastly footprints of a gigantic hound are discovered near his body. Sherlock Holmes and Dr. John Watson are engaged to protect the young heir, Sir Henry, unraveling ancient legends, eerie midnight signals, and treacherous bog mire.",
            "9780140437867",
            4.7,
            490,
            1902,
            256,
            "English",
            List.of(
                "Sherlock Holmes", "Dartmoor", "Family Curse", "Atmospheric Mystery"),
            false,
            94,
            review(
                "Dr. Watson Fan",
                "August 14, 2025",
                5,
                "The atmosphere of the foggy Dartmoor moor is as thrilling as Holmes's deductions."),
            review(
                "Eleanor Wright",
                "September 23, 2025",
                5,
                "Supernatural dread meets Victorian scientific logic in perfect harmony."),
            review(
                "Simon Hayes",
                "October 19, 2025",
                4,
                "Watson carries the middle section with wonderful courage and observation.")));

    list.add(
        book(
            12L,
            "And Then There Were None",
            "Agatha Christie",
            "Mystery",
            "Ten strangers with guilty pasts are invited to an opulent mansion on a secluded Devonshire island by an enigmatic host who never appears. Cut off from the mainland by a fierce tempest, the guests are marked for execution one by one in uncanny accordance with a sinister childhood poem.",
            "9780062073488",
            4.9,
            620,
            1939,
            272,
            "English",
            List.of("Locked Room", "Isolated Island", "Ten Strangers", "Nursery Rhyme"),
            false,
            98,
            review(
                "Beatrice Shaw",
                "July 29, 2025",
                5,
                "Flawless clockwork suspense. The psychological paranoia keeps you guessing until the epilogue."),
            review(
                "Marcus Sterling",
                "September 18, 2025",
                5,
                "Arguably the greatest mystery novel ever written. Cold, ingenious, and relentless."),
            review(
                "Helena Troy",
                "October 27, 2025",
                5,
                "The ticking nursery rhyme countdown creates unbearable, delicious tension.")));

    list.add(
        book(
            13L,
            "The Moonstone",
            "Wilkie Collins",
            "Mystery",
            "On her eighteenth birthday, Rachel Verinder inherits an exquisite, cursed yellow diamond stolen from an Indian shrine, only for the jewel to vanish from her boudoir overnight. Narrated through the interlocking testimonies of bewildered witnesses, Sergeant Cuff investigates an intricate tapestry of addiction, somnambulism, and hidden romance.",
            "9780140434088",
            4.4,
            210,
            1868,
            528,
            "English",
            List.of(
                "Victorian Detective", "Stolen Diamond", "Multiple Viewpoints", "Sergeant Cuff"),
            false,
            80,
            review(
                "Edmund Burke",
                "June 17, 2025",
                4,
                "T.S. Eliot called it the first and best of modern detective stories, and he was right."),
            review(
                "Victoria S.",
                "August 22, 2025",
                5,
                "Betteredge's narration with his beloved Robinson Crusoe is pure literary joy."),
            review(
                "Leonard Moss",
                "October 14, 2025",
                4,
                "Intricate plotting with sharp Victorian social commentary throughout.")));

    list.add(
        book(
            14L,
            "The Woman in White",
            "Wilkie Collins",
            "Mystery",
            "While walking home on a moonlit London road, drawing master Walter Hartright encounters an agitated young woman dressed entirely in white, escaped from a nearby asylum. When he is hired to tutor the wealthy Laura Fairlie, Walter discovers an uncanny resemblance between Laura and the apparition, plunging into a villainous conspiracy of forged documents and stolen lives.",
            "9780141439617",
            4.6,
            280,
            1859,
            672,
            "English",
            List.of(
                "Gothic Mystery", "Secret Identities", "Asylum", "Victorian Sensation"),
            false,
            84,
            review(
                "Marian Halcombe Admirer",
                "July 15, 2025",
                5,
                "Marian Halcombe is one of the bravest, sharpest heroines in Victorian literature."),
            review(
                "Raymond Finch",
                "September 9, 2025",
                5,
                "Count Fosco is a devilishly cultured villain. The epistolary format works wonders."),
            review(
                "Cordelia Grey",
                "November 3, 2025",
                4,
                "A grand Victorian sensation novel that never loses its dramatic momentum.")));

    list.add(
        book(
            15L,
            "The Thursday Murder Club",
            "Richard Osman",
            "Mystery",
            "In a serene retirement village in the Kent countryside, four unorthodox septuagenarians meet every Thursday in the Jigsaw Room to investigate unsolved cold cases. When a local property developer is discovered murdered with a photograph left beside his corpse, the club finds itself investigating a live murder with dry wit and ruthless determination.",
            "9781984880987",
            4.6,
            480,
            2020,
            382,
            "English",
            List.of(
                "Cozy Mystery", "Retirement Village", "Witty Sleuths", "British Humour"),
            false,
            92,
            review(
                "Penelope Cruz-White",
                "August 18, 2025",
                5,
                "Warm, exceptionally witty, and full of surprising emotional depth about aging."),
            review(
                "Douglas Kerr",
                "October 3, 2025",
                5,
                "Elizabeth and Joyce make the most charming investigative duo in years."),
            review(
                "Sarah Connolly",
                "November 8, 2025",
                4,
                "A delightful English whodunit with clever twists and wonderful dialogue.")));

    list.add(
        book(
            16L,
            "The Maid",
            "Nita Prose",
            "Mystery",
            "Molly Gray is a fastidious hotel maid who takes immense pride in restoring luxury suites to pristine perfection at the opulent Regency Grand. When she discovers the infamous tycoon Charles Black dead in his bed, Molly's idiosyncratic social perceptions make her the primary police suspect, forcing her loyal friends to help clear her name.",
            "9780593356159",
            4.3,
            360,
            2022,
            304,
            "English",
            List.of(
                "Locked Room Hotel", "Neurodivergent Sleuth", "Heartwarming", "Grand Hotel"),
            false,
            86,
            review(
                "Claire Dupont",
                "September 5, 2025",
                4,
                "Molly's unique voice is enchanting. Her grandma's wisdom gives the story a warm soul."),
            review(
                "Oliver West",
                "October 12, 2025",
                4,
                "Charming locked-room mystery with Agatha Christie nods and genuine tenderness."),
            review(
                "Naomi Tanaka",
                "November 6, 2025",
                5,
                "A cozy, feel-good whodunit that champions kindness and quiet integrity.")));

    list.add(
        book(
            17L,
            "Magpie Murders",
            "Anthony Horowitz",
            "Mystery",
            "Book editor Susan Ryeland sits down to review the handwritten manuscript of bestselling author Alan Conway's newest Atticus Pünd whodunit. She soon realizes that the final chapter containing the killer's identity is missing, and within hours, Conway himself is found dead in his estate gardens under eerily parallel circumstances.",
            "9780062645227",
            4.5,
            270,
            2016,
            496,
            "English",
            List.of(
                "Book Within a Book", "Literary Puzzle", "Murder Mystery", "Golden Age Tribute"),
            false,
            85,
            review(
                "Felix Harper",
                "July 31, 2025",
                5,
                "A love letter to classic English murder mysteries. The dual narrative is genius."),
            review(
                "Agnes Mortimer",
                "September 27, 2025",
                4,
                "Horowitz constructs anagrams and clues like a puzzle master at the height of his craft."),
            review(
                "Tobias Green",
                "October 24, 2025",
                5,
                "Smart, meta-fictional, and completely absorbing from first manuscript to last.")));

    list.add(
        book(
            18L,
            "In the Woods",
            "Tana French",
            "Mystery",
            "Detective Rob Ryan investigates the tragic murder of a twelve-year-old girl in the forest outside Dublin, the exact woods where two decades earlier, two of Ryan's childhood companions vanished without a trace. Haunted by his own shattered memory, Ryan struggles to maintain his sanity as the present crime mirrors his buried trauma.",
            "9780143113492",
            4.4,
            310,
            2007,
            429,
            "English",
            List.of(
                "Dublin Murder Squad", "Psychological Mystery", "Haunted Past", "Atmospheric"),
            false,
            83,
            review(
                "Deirdre O'Connor",
                "August 7, 2025",
                5,
                "Atmospheric, poetic, and psychologically searing. French captures Ireland's misty grief."),
            review(
                "Patrick Brennan",
                "October 1, 2025",
                4,
                "The partnership between Rob and Cassie is complex, witty, and heartbreaking."),
            review(
                "Maeve Kelly",
                "November 12, 2025",
                4,
                "A haunting, literary mystery that refuses to tie up darkness in a neat bow.")));

    // =========================================================================
    // 3. THRILLER (IDs 19 - 27)
    // =========================================================================
    list.add(
        book(
            19L,
            "The Girl with the Dragon Tattoo",
            "Stieg Larsson",
            "Thriller",
            "Disgraced financial journalist Mikael Blomkvist is hired by an industrial patriarch to solve the forty-year-old disappearance of his beloved grandniece from a secluded Swedish island. Teaming up with brilliant, fiercely guarded hacker Lisbeth Salander, the pair uncovers a legacy of corruption, family atrocities, and serial violence hidden beneath high society.",
            "9780307454541",
            4.7,
            540,
            2005,
            465,
            "English (translated from Swedish)",
            List.of(
                "Nordic Noir", "Lisbeth Salander", "Investigative Journalism", "Dark Conspiracy"),
            true,
            96,
            review(
                "Erik Lindqvist",
                "June 25, 2025",
                5,
                "Lisbeth Salander is an icon of justice and sheer defiance. Unforgettable Nordic noir."),
            review(
                "Karin V.",
                "August 30, 2025",
                5,
                "Meticulous, dark, and utterly gripping. The Vanger family tree is a labyrinth of sin."),
            review(
                "Markus Vance",
                "October 20, 2025",
                4,
                "A slow-burn investigative thriller that explodes into relentless action.")));

    list.add(
        book(
            20L,
            "Gone Girl",
            "Gillian Flynn",
            "Thriller",
            "On the morning of their fifth wedding anniversary in Missouri, beautiful and clever Amy Dunne vanishes without a trace from her lakeside home. As police scrutiny and sensational media headlines mount, her husband Nick's web of evasions, diary revelations, and calculated behavior make everyone wonder if he murdered his wife.",
            "9780307588371",
            4.6,
            590,
            2012,
            422,
            "English",
            List.of(
                "Twisted Marriage",
                "Unreliable Narrator",
                "Media Circus",
                "Psychological Thriller"),
            false,
            97,
            review(
                "Chloe Bennett",
                "July 19, 2025",
                5,
                "The midway twist altered domestic thrillers forever. Razor-sharp and cynical."),
            review(
                "Derek Hayes",
                "September 11, 2025",
                5,
                "Flynn's dissection of modern marriage and media manipulation is savagely brilliant."),
            review(
                "Samantha Ray",
                "November 4, 2025",
                4,
                "You won't root for either of them, but you will not be able to look away.")));

    list.add(
        book(
            21L,
            "The Da Vinci Code",
            "Dan Brown",
            "Thriller",
            "While visiting Paris, Harvard symbologist Robert Langdon is summoned to the Louvre Museum after the elderly curator is found murdered in a symbolic pose reminiscent of Da Vinci's Vitruvian Man. Fleeing French police alongside cryptologist Sophie Neveu, Langdon deciphers hidden riddles concealed in classic Renaissance masterworks to safeguard a millennia-old secret.",
            "9780307474278",
            4.3,
            560,
            2003,
            454,
            "English",
            List.of("Symbology", "Louvre Mystery", "Secret Societies", "High Octane"),
            false,
            93,
            review(
                "Professor Laurent",
                "May 15, 2025",
                4,
                "A breathless treasure hunt through Parisian history and hidden cryptograms."),
            review(
                "Gary Cooper",
                "August 8, 2025",
                5,
                "Pure page-turning adrenaline with short chapters that keep you reading until 3 AM."),
            review(
                "Monique Dubois",
                "October 10, 2025",
                4,
                "Fascinating historical puzzles woven into an electrifying chase across Europe.")));

    list.add(
        book(
            22L,
            "The Spy Who Came in from the Cold",
            "John le Carre",
            "Thriller",
            "Alec Leamas is an aging British intelligence officer who has witnessed his last Berlin operative gunned down at Checkpoint Charlie. Before he can retire, the Circus asks him to perform one final, deceitful mission: pose as an embittered alcoholic defector in East Germany, only to find himself a pawn in a far more ruthless geopolitical deception.",
            "9780143124757",
            4.6,
            290,
            1963,
            224,
            "English",
            List.of("Cold War Espionage", "Berlin Wall", "Moral Ambiguity", "Realism"),
            false,
            88,
            review(
                "George Smiley Admirer",
                "June 28, 2025",
                5,
                "The anti-James Bond masterpiece. Cold, grey, gritty, and morally devastating."),
            review(
                "Warren Mitchell",
                "August 19, 2025",
                4,
                "Le Carré understands the psychological toll of state-sponsored deception."),
            review(
                "Irene Adler",
                "October 25, 2025",
                5,
                "The climax at the Berlin Wall is one of the most chilling in spy literature.")));

    list.add(
        book(
            23L,
            "The Talented Mr. Ripley",
            "Patricia Highsmith",
            "Thriller",
            "Struggling New York forger Tom Ripley is commissioned by a wealthy shipping magnate to travel to Italy and persuade his wayward son Dickie Greenleaf to return home. Enamored by Dickie's sun-drenched European lifestyle and carefree elegance, Tom's sycophancy curdles into obsession, leading to a calculated murder and an audacious assumption of another man's identity.",
            "9780393332148",
            4.5,
            340,
            1955,
            288,
            "English",
            List.of("Sociopath", "Italian Riviera", "Identity Theft", "Dark Suspense"),
            false,
            89,
            review(
                "Vivian Blake",
                "July 12, 2025",
                5,
                "Highsmith forces you into the shoes of a charming sociopath and keeps you complicit."),
            review(
                "Marco Rossi",
                "September 17, 2025",
                4,
                "Sun-drenched Italian glamour curdles into icy paranoia with effortless poise."),
            review(
                "Geraldine Cross",
                "November 9, 2025",
                5,
                "Tom Ripley is hypnotic. You find yourself sweating as he hides his tracks.")));

    list.add(
        book(
            24L,
            "Shutter Island",
            "Dennis Lehane",
            "Thriller",
            "In the summer of 1954, U.S. Marshal Teddy Daniels and his new partner arrive at Ashecliffe Hospital on Shutter Island to investigate the impossible escape of a murderous patient from a locked chamber. As a violent hurricane cuts off communication with the mainland, Teddy realizes the fortress hospital conceals unethical psychiatric experiments and shocking truths about his own past.",
            "9780061898815",
            4.7,
            440,
            2003,
            385,
            "English",
            List.of("Asylum", "Gothic Island", "Twist Ending", "Investigative"),
            false,
            94,
            review(
                "Calvin Brooks",
                "May 22, 2025",
                5,
                "The oppressive island atmosphere and torrential rain soak every single sentence."),
            review(
                "Dana Scully",
                "August 13, 2025",
                5,
                "A devastating psychological puzzle where paranoia and reality bleed together."),
            review(
                "Liam Vance",
                "October 17, 2025",
                4,
                "One of Lehane's best. The lighthouse sequence is unforgettable.")));

    list.add(
        book(
            25L,
            "Before I Go to Sleep",
            "S. J. Watson",
            "Thriller",
            "Christine Lucas wakes up every single morning in an unfamiliar bedroom alongside a man she does not recognize, believing herself to be an unburdened twenty-something woman. A doctor contacts her in secret to remind her of a hidden diary where she has inscribed four chilling words: 'Do not trust Ben.'",
            "9780062060563",
            4.2,
            290,
            2011,
            368,
            "English",
            List.of("Amnesia", "Memory Loss", "Secret Journal", "Domestic Suspense"),
            false,
            81,
            review(
                "Hannah Mills",
                "June 9, 2025",
                4,
                "A terrifying premise executed with relentless domestic tension and dread."),
            review(
                "Geoffrey Price",
                "August 29, 2025",
                4,
                "You feel Christine's disorientation intimately. The diary conceit works flawlessly."),
            review(
                "Tessa Scott",
                "October 28, 2025",
                5,
                "Creepy, claustrophobic, and kept me guessing until the final harrowing reveal.")));

    list.add(
        book(
            26L,
            "Red Dragon",
            "Thomas Harris",
            "Thriller",
            "Gifted FBI profiler Will Graham comes out of voluntary retirement to track an elusive serial killer known as the Tooth Fairy, who butchers whole families on consecutive full moons. To penetrate the killer's psychotic mind, Graham must consult with his former tormentor, the incarcerated cannibalistic genius Dr. Hannibal Lecter.",
            "9780425228227",
            4.7,
            370,
            1981,
            454,
            "English",
            List.of("FBI Profiling", "Hannibal Lecter", "Serial Killer", "Dark Procedural"),
            false,
            92,
            review(
                "Special Agent Fox",
                "July 27, 2025",
                5,
                "Will Graham's empathy is a terrifying curse. Harris created modern forensic fiction."),
            review(
                "Clara Oswald",
                "September 22, 2025",
                5,
                "Hannibal Lecter's introduction is quiet, magnetic, and profoundly sinister."),
            review(
                "Martin Shaw",
                "November 11, 2025",
                4,
                "Deeply psychological with an antagonist whose tragic humanity makes him scarier.")));

    list.add(
        book(
            27L,
            "The Day of the Jackal",
            "Frederick Forsyth",
            "Thriller",
            "In the turbulent 1960s, a militant French underground organization hires an aristocratic British contract killer known only as 'The Jackal' to assassinate President Charles de Gaulle. As the methodical assassin constructs customized rifles and fake identities across Europe, a dogged French police commissioner races against the clock to decipher his phantom movements.",
            "9780451207395",
            4.6,
            260,
            1971,
            358,
            "English",
            List.of(
                "Political Assassination", "Contract Killer", "Cat and Mouse", "Precision Plotting"),
            false,
            87,
            review(
                "Major Sterling",
                "June 11, 2025",
                5,
                "The gold standard for forensic cat-and-mouse thrillers. Surgical precision in every chapter."),
            review(
                "Jean-Luc Morel",
                "August 20, 2025",
                4,
                "Even knowing real history, Forsyth makes your heart race as the sniper takes aim."),
            review(
                "Simon Vance",
                "October 14, 2025",
                5,
                "A clinic in plotting, false identities, and bureaucratic investigation.")));

    // =========================================================================
    // 4. HORROR (IDs 28 - 36)
    // =========================================================================
    list.add(
        book(
            28L,
            "Dracula",
            "Bram Stoker",
            "Horror",
            "Solicitor Jonathan Harker travels to the misty Carpathian mountains of Transylvania to finalize a London real estate deal for the eccentric Count Dracula, only to discover himself imprisoned in a castle of the undead. As the nocturnal predator sails to Victorian England bringing pestilence and terror, an alliance led by Professor Van Helsing rallies to destroy him.",
            "9780141439846",
            4.8,
            580,
            1897,
            418,
            "English",
            List.of("Vampire Gothic", "Transylvania", "Epistolary", "Count Dracula"),
            true,
            98,
            review(
                "Mina Murray Fan",
                "September 14, 2025",
                5,
                "The epistolary format makes the terror feel intimate, urgent, and profoundly real."),
            review(
                "Julian Croft",
                "October 24, 2025",
                5,
                "The opening chapters in Castle Dracula are the greatest gothic horror ever penned."),
            review(
                "Helena Drake",
                "November 1, 2025",
                4,
                "An enduring classic that invented modern vampire lore with unmatched dread.")));

    list.add(
        book(
            29L,
            "Frankenstein",
            "Mary Shelley",
            "Horror",
            "Driven by obsessive ambition to conquer death itself, brilliant Swiss scholar Victor Frankenstein stitches together discarded charnel flesh and animates a sapient being with electrical spark. Horrified by the grotesque sight of his creation, Victor abandons the monster, unleashing a tragic cycle of loneliness, vengeance, and ruin across icy frontiers.",
            "9780141439471",
            4.7,
            530,
            1818,
            280,
            "English",
            List.of(
                "Gothic Horror", "Mad Scientist", "Creature", "Existential Tragedy"),
            false,
            96,
            review(
                "Percy B.",
                "August 17, 2025",
                5,
                "Written by an 18-year-old visionary who birthed science fiction and gothic tragedy."),
            review(
                "Rowena Stark",
                "October 11, 2025",
                5,
                "The creature's articulate lament in the Mer de Glace breaks your heart."),
            review(
                "Anthony Black",
                "November 7, 2025",
                4,
                "A philosophical masterpiece asking who is the true monster: maker or creation?")));

    list.add(
        book(
            30L,
            "The Haunting of Hill House",
            "Shirley Jackson",
            "Horror",
            "Dr. John Montague invites four strangers with supernatural sensitivities to spend a summer observing paranormal phenomena inside the ominous, architecturally warped Hill House. Sensitive, lonely Eleanor Vance finds herself deeply attuned to the brooding manor's creaking staircases and quiet whispers, as the house prepares to claim a permanent resident.",
            "9780143039983",
            4.7,
            460,
            1959,
            246,
            "English",
            List.of("Haunted House", "Psychological Terror", "Eleanor Vance", "Atmospheric"),
            false,
            93,
            review(
                "Eleanor Vance Fan",
                "July 21, 2025",
                5,
                "'Whatever walked there, walked alone.' The greatest opening paragraph in literature."),
            review(
                "Silas Thorne",
                "September 19, 2025",
                5,
                "Jackson terrifies not with gore, but with psychological isolation and locked doors."),
            review(
                "Cassandra Grey",
                "October 30, 2025",
                4,
                "A sublime, eerie study of vulnerability and a house that consumes the soul.")));

    list.add(
        book(
            31L,
            "The Shining",
            "Stephen King",
            "Horror",
            "Aspiring playwright Jack Torrance accepts a winter caretaker position at the isolated Overlook Hotel high in the Colorado Rockies, bringing along his wife Wendy and gifted son Danny. As blizzards cut off the mountain passes, the hotel's malevolent spectral history preys on Jack's fragile sobriety, driving him toward homicidal madness.",
            "9780307743657",
            4.9,
            640,
            1977,
            447,
            "English",
            List.of(
                "Overlook Hotel", "Cabin Fever", "Telepathy", "Supernatural Possession"),
            false,
            99,
            review(
                "Wendy Torrance Ally",
                "June 23, 2025",
                5,
                "King's deepest exploration of addiction and family tragedy wrapped in pure terror."),
            review(
                "Dorian Gray Fan",
                "August 27, 2025",
                5,
                "The hedge animals and Room 217 will haunt your dreams for weeks."),
            review(
                "Tyler Vance",
                "October 19, 2025",
                5,
                "Infinitely more heartbreaking and atmospheric than the film adaptation.")));

    list.add(
        book(
            32L,
            "Pet Sematary",
            "Stephen King",
            "Horror",
            "Dr. Louis Creed moves his young family to a charming rural home in Maine, bordered by a highway where speeding logging trucks thunder past. Behind their property lies a pathway leading to a makeshift pet graveyard—and farther into the woods, an ancient, sour burial ground that returns the dead in horrifyingly twisted forms.",
            "9781501156700",
            4.6,
            430,
            1983,
            374,
            "English",
            List.of("Grief & Death", "Ancient Burial Ground", "Resurrection", "Macabre"),
            false,
            92,
            review(
                "Judson Crandall",
                "July 14, 2025",
                5,
                "'Sometimes, dead is better.' The most unflinchingly terrifying book King ever wrote."),
            review(
                "Maggie Miller",
                "September 28, 2025",
                4,
                "A visceral examination of parental grief that spirals into cosmic horror."),
            review(
                "Nathan Cole",
                "October 22, 2025",
                5,
                "Dark, mournful, and unrelenting. The ending gave me absolute chills.")));

    list.add(
        book(
            33L,
            "Bird Box",
            "Josh Malerman",
            "Horror",
            "An unfathomable entity has infested the globe; anyone who catches even a glimpse of it is instantly driven to gruesome, violent suicide. Malorie and two small children must navigate twenty miles down a perilous river in a rowboat completely blindfolded, relying solely on acute hearing to evade horrors lurking upon the banks.",
            "9780062259653",
            4.3,
            370,
            2014,
            262,
            "English",
            List.of("Sensory Horror", "Apocalyptic", "Blindfold", "Unseen Threat"),
            false,
            87,
            review(
                "Blindfold Reader",
                "August 12, 2025",
                4,
                "The sensory deprivation makes your skin crawl. You hear every twig snapping."),
            review(
                "Brendan Fox",
                "September 30, 2025",
                4,
                "A taut, fast-paced apocalyptic survival nightmare with genuine heart."),
            review(
                "Lydia Stone",
                "November 3, 2025",
                5,
                "Terrifying precisely because you never see the monsters. Pure primal fear.")));

    list.add(
        book(
            34L,
            "Mexican Gothic",
            "Silvia Moreno-Garcia",
            "Horror",
            "Glamorous socialite Noemí Taboada travels to High Place, an isolated mansion nestled in the misty Mexican mountains, after receiving a frantic, incoherent letter from her newlywed cousin. Within its rotting English wallpaper and eerie silence, Noemí encounters an arrogant colonial family nursing sinister fungal ties to the earth beneath their feet.",
            "9780525620785",
            4.5,
            410,
            2020,
            301,
            "English",
            List.of(
                "High Place", "1950s Mexico", "Fungal Horror", "Decaying Aristocracy"),
            false,
            91,
            review(
                "Catalina Fan",
                "June 30, 2025",
                5,
                "Fabulous 1950s gothic atmosphere. Noemí is bold, smart, and a joy to follow."),
            review(
                "Mateo Hernandez",
                "August 25, 2025",
                5,
                "The fungal mythology combined with decadent Victorian horror is utterly unique."),
            review(
                "Sophia Chen",
                "October 15, 2025",
                4,
                "Dripping with rot, golden mist, and creepy family secrets. Excellent gothic read.")));

    list.add(
        book(
            35L,
            "Hell House",
            "Richard Matheson",
            "Horror",
            "A dying billionaire hires a physicist, a spiritualist, and the sole survivor of a previous ill-fated investigation to enter the notorious Belasco House and prove the existence of an afterlife. Known as the Mount Everest of haunted houses, the manor unleashes sexual degradation, telekinetic assaults, and psychological torture upon the scientists.",
            "9780312868857",
            4.4,
            240,
            1971,
            301,
            "English",
            List.of(
                "Belasco House", "Paranormal Expedition", "Visceral Terror", "Haunted"),
            false,
            82,
            review(
                "Arthur Barrett",
                "July 5, 2025",
                4,
                "Visceral, intense, and relentless. Matheson pits science against ancient depravity."),
            review(
                "Gail Montgomery",
                "September 16, 2025",
                4,
                "The pacing never lets up. The Belasco House feels actively malevolent."),
            review(
                "Evan Hughes",
                "November 10, 2025",
                5,
                "A foundational haunted-house novel that influenced a generation of writers.")));

    list.add(
        book(
            36L,
            "Ghost Story",
            "Peter Straub",
            "Horror",
            "In the sleepy snowy town of Milburn, New York, four elderly gentlemen of the Chowder Society gather each winter to share eerie ghost tales and forget their own mortality. But an unforgivable sin committed fifty years earlier refuses to stay buried, returning in the guise of a seductive phantom to punish the town.",
            "9781101989043",
            4.4,
            230,
            1979,
            512,
            "English",
            List.of("Chowder Society", "Winter Town", "Vengeful Spirit", "Dark Classic"),
            false,
            81,
            review(
                "Chowder Society Guest",
                "August 4, 2025",
                5,
                "Lush, literary gothic horror wrapped in blankets of upstate New York snow."),
            review(
                "Russell Vance",
                "October 5, 2025",
                4,
                "Straub writes with elegance and terrifying patient escalation."),
            review(
                "Deborah Lynn",
                "November 14, 2025",
                4,
                "A rich ghost story about guilt, aging, and the secrets that haunt small towns.")));

    // =========================================================================
    // 5. ROMANCE (IDs 37 - 45)
    // =========================================================================
    list.add(
        book(
            37L,
            "Pride and Prejudice",
            "Jane Austen",
            "Romance",
            "The clever and spirited Elizabeth Bennet is determined to marry solely for genuine affection despite society's relentless pressures and her mother's matrimonial schemes. When she encounters the proud, aloof aristocrat Mr. Fitzwilliam Darcy, initial misunderstandings and sharp repartee mask a profound devotion that transforms them both.",
            "9780141439518",
            4.9,
            630,
            1813,
            279,
            "English",
            List.of(
                "Regency Romance", "Enemies to Lovers", "Elizabeth Bennet", "Mr. Darcy"),
            true,
            99,
            review(
                "Lizzie Bennet Fan",
                "July 11, 2025",
                5,
                "The blueprint for romantic comedy. Austen's wit, dialogue, and irony remain eternal."),
            review(
                "Charles Bingley",
                "September 2, 2025",
                5,
                "Darcy's letter and the visit to Pemberley are romantic perfection."),
            review(
                "Emmeline Frost",
                "October 28, 2025",
                5,
                "I reread this every single autumn. The banter is unparalleled.")));

    list.add(
        book(
            38L,
            "Persuasion",
            "Jane Austen",
            "Romance",
            "Eight years after being persuaded by friends to break off her engagement to a penniless naval officer, Anne Elliot lives quietly with regret and fading youth. When Captain Frederick Wentworth returns wealthy and decorated from war, the rekindling of unspoken love tests whether true devotion can outlast time and societal pride.",
            "9780141439686",
            4.8,
            420,
            1817,
            249,
            "English",
            List.of(
                "Second Chance Romance", "Regency", "Captain Wentworth", "Patience"),
            false,
            94,
            review(
                "Anne Elliot Admirer",
                "August 16, 2025",
                5,
                "Wentworth's letter—'You pierce my soul. I am half agony, half hope'—is the greatest in history."),
            review(
                "Lady Russell",
                "October 9, 2025",
                5,
                "Austen's most mature, tender, and melancholic love story."),
            review(
                "Graham Sterling",
                "November 6, 2025",
                5,
                "The quiet longing in every glance across crowded rooms is breathtaking.")));

    list.add(
        book(
            39L,
            "North and South",
            "Elizabeth Gaskell",
            "Romance",
            "Southern gentlewoman Margaret Hale is uprooted from her idyllic agrarian home and moved to the smoky northern manufacturing town of Milton. There she clashes fiercely with brooding mill owner John Thornton, igniting a powerful battle of social conscience and intense romantic longing amidst labor strikes.",
            "9780140434248",
            4.6,
            280,
            1855,
            451,
            "English",
            List.of(
                "Victorian Romance", "Industrial Mill", "Class Clash", "Margaret Hale"),
            false,
            86,
            review(
                "Margaret Hale Supporter",
                "June 21, 2025",
                5,
                "A romance with real political and economic teeth. Thornton and Margaret are unforgettable."),
            review(
                "Tobias Thornton",
                "August 29, 2025",
                5,
                "The tension during the riot scene and the proposal scene is unmatched."),
            review(
                "Felicity Ward",
                "October 21, 2025",
                4,
                "Gaskell tackles industrial reality while crafting a deeply passionate slow burn.")));

    list.add(
        book(
            40L,
            "The Blue Castle",
            "L. M. Montgomery",
            "Romance",
            "Meek twenty-nine-year-old Valancy Stirling has spent her entire life cowed by her overbearing relatives in a dreary Canadian town. Upon receiving a doctor's diagnosis granting her only one year to live, Valancy rebels, speaking her candid mind and escaping into the fragrant pine woods alongside mysterious outlaw Barney Snaith.",
            "9781402289365",
            4.7,
            260,
            1926,
            218,
            "English",
            List.of("Cozy Romance", "Canadian Woods", "Self-Discovery", "Valancy Stirling"),
            false,
            88,
            review(
                "Valancy Fan",
                "July 26, 2025",
                5,
                "Pure comfort reading. Watching Valancy tell off her miserable family is deeply satisfying."),
            review(
                "Pine Woods Wanderer",
                "September 12, 2025",
                5,
                "Barney Snaith is the dream hero: books, nature, a cozy cabin, and true respect."),
            review(
                "Clara Belle",
                "November 4, 2025",
                4,
                "An enchanting, feel-good classic of courage, love, and living on your own terms.")));

    list.add(
        book(
            41L,
            "The Flatshare",
            "Beth O'Leary",
            "Romance",
            "Tiffy Moore desperately needs a cheap flat in London, and night-shift hospice nurse Leon Twomey needs extra cash. They agree to share a one-bedroom apartment without ever meeting in person—Leon sleeps by day, Tiffy occupies the space by night—communicating through a colorful flurry of handwritten Post-it notes until affection blossoms.",
            "9781250295637",
            4.5,
            460,
            2019,
            336,
            "English",
            List.of(
                "Contemporary Romance", "Opposites Attract", "Post-It Notes", "London Life"),
            false,
            91,
            review(
                "Tiffy & Leon Fan",
                "August 1, 2025",
                5,
                "The Post-it note correspondence is utterly charming, warm, and romantic."),
            review(
                "Marcus Vance",
                "September 24, 2025",
                4,
                "Sweet and funny, but also treats emotional recovery from past trauma with care."),
            review(
                "Gemma Taylor",
                "October 29, 2025",
                5,
                "A modern classic of the genre with two lead characters you adore instantly.")));

    list.add(
        book(
            42L,
            "Book Lovers",
            "Emily Henry",
            "Romance",
            "Fierce literary agent Nora Stephens has read enough manuscripts to know she is the prickly girlfriend who always gets dumped for the sweet small-town baker. When she takes a summer vacation with her sister to tiny Sunshine Falls, she repeatedly bumps into Charlie Lastra, the hypercritical book editor who knows her heart better than anyone.",
            "9780593334836",
            4.6,
            490,
            2022,
            384,
            "English",
            List.of(
                "Literary Romance",
                "Rival Editors",
                "Small Town Subversion",
                "Enemies to Lovers"),
            false,
            93,
            review(
                "Nora Stephens Fan",
                "May 18, 2025",
                5,
                "Subverts every city-girl romance trope with sharp wit and huge heart."),
            review(
                "Charlie Lastra Admirer",
                "August 19, 2025",
                5,
                "The banter between two cynical publishing professionals is razor-sharp and joyful."),
            review(
                "Hannah Morris",
                "October 13, 2025",
                4,
                "Emily Henry's best book. The sister relationship is just as moving as the romance.")));

    list.add(
        book(
            43L,
            "Beach Read",
            "Emily Henry",
            "Romance",
            "A disillusioned romance novelist grieving her father's passing and a cynical literary fiction writer suffering severe writer's block rent neighboring beach houses on Lake Michigan. To break their creative slumps, the bitter former college rivals strike a wager: he will attempt a joyful happily-ever-after romance, while she writes the next great depressing American drama.",
            "9781984806734",
            4.5,
            470,
            2020,
            361,
            "English",
            List.of("Writers Romance", "Summer Cottage", "Banter", "Healing"),
            false,
            92,
            review(
                "January Andrews",
                "June 27, 2025",
                5,
                "Rich, deeply emotional, and sexy. The research trips they take for each other are wonderful."),
            review(
                "Gus Everett Club",
                "September 10, 2025",
                4,
                "Deals with grief, infidelity, and artistic doubt with remarkable warmth."),
            review(
                "Olivia Lane",
                "November 7, 2025",
                5,
                "The note-passing through the windows sold me completely. A summer staple.")));

    list.add(
        book(
            44L,
            "The Love Hypothesis",
            "Ali Hazelwood",
            "Romance",
            "Third-year biology Ph.D. candidate Olive Smith panics to convince her best friend that she is happily dating, kissing the first man she sees in a university hallway. Unfortunately, that man happens to be Dr. Adam Carlsen, the brilliant, tyrannical young professor who unexpectedly agrees to play her fake boyfriend.",
            "9780593336823",
            4.5,
            510,
            2021,
            356,
            "English",
            List.of("STEM Romance", "Fake Dating", "Grumpy x Sunshine", "Academia"),
            false,
            94,
            review(
                "Olive & Adam",
                "July 23, 2025",
                5,
                "Grumpy-meets-sunshine academic perfection. Adam Carlsen is a top-tier book boyfriend."),
            review(
                "Dr. Science",
                "September 15, 2025",
                4,
                "Captures the chaotic panic of graduate school and research lab politics perfectly."),
            review(
                "Kylie Brooks",
                "October 26, 2025",
                5,
                "Funny, spicy, and thoroughly addictive. Read it in one sitting.")));

    list.add(
        book(
            45L,
            "Red, White & Royal Blue",
            "Casey McQuiston",
            "Romance",
            "Alex Claremont-Diaz, the charming First Son of the United States, harbors a longstanding feud with Britain's aloof Prince Henry. After a public disaster with an imperial wedding cake threatens international relations, the two rivals are coerced into a staged truce that gradually evolves into a secret, earth-shaking trans-Atlantic romance.",
            "9781250316776",
            4.6,
            530,
            2019,
            421,
            "English",
            List.of(
                "Queer Royalty", "First Son & Prince", "Enemies to Lovers", "Political Fluff"),
            false,
            95,
            review(
                "History Huh Fan",
                "August 15, 2025",
                5,
                "'History, huh? Bet we could make some.' An optimistic, hilarious, and gorgeous romance."),
            review(
                "Henry Fox-Mountchristen",
                "September 29, 2025",
                5,
                "Their email correspondence quotes classic literature and aches with honest devotion."),
            review(
                "Julian Croft",
                "November 11, 2025",
                4,
                "Bursting with hope, clever dialogue, and joyful, passionate representation.")));

    // =========================================================================
    // 6. DARK ROMANCE (IDs 46 - 54)
    // =========================================================================
    list.add(
        book(
            46L,
            "Wuthering Heights",
            "Emily Bronte",
            "Dark Romance",
            "Across the windswept Yorkshire moors, the tempestuous bond between foundling Heathcliff and free-spirited Catherine Earnshaw spirals into consuming vengeance after Catherine chooses social respectability. Heathcliff's vengeful wrath spans generations, haunting the lonely stone chambers of Wuthering Heights beyond the grave.",
            "9780141439556",
            4.6,
            560,
            1847,
            416,
            "English",
            List.of(
                "Gothic Moors", "Heathcliff & Catherine", "Tormented Longing", "Revenge"),
            true,
            96,
            review(
                "Yorkshire Moor Wanderer",
                "July 17, 2025",
                5,
                "The rawest, wildest exploration of obsession ever written. It belongs to wind and heather."),
            review(
                "Nelly Dean Confidant",
                "September 8, 2025",
                4,
                "Fierce, feral, and uncompromising. Heathcliff is the definitive antihero."),
            review(
                "Evelyn Cross",
                "October 25, 2025",
                5,
                "'Whatever our souls are made of, his and mine are the same.' Untamable genius.")));

    list.add(
        book(
            47L,
            "Jane Eyre",
            "Charlotte Bronte",
            "Dark Romance",
            "An orphaned and independent young governess arrives at Thornfield Hall to teach the ward of the brooding, enigmatic Edward Rochester. As romantic tension and intellectual kinship kindle between them, the terrifying secret concealed in Thornfield's locked third-floor attic threatens to destroy their world.",
            "9780141441146",
            4.8,
            600,
            1847,
            507,
            "English",
            List.of(
                "Gothic Governess", "Mr. Rochester", "Thornfield Hall", "Moral Strength"),
            false,
            98,
            review(
                "Jane Eyre Admirer",
                "August 5, 2025",
                5,
                "Jane's fierce independence and moral integrity make this the ultimate gothic romance."),
            review(
                "Edward Rochester Fan",
                "September 26, 2025",
                5,
                "Thornfield Hall's eerie laughter and stormy grounds are peak atmospheric brilliance."),
            review(
                "Lucinda Drake",
                "November 2, 2025",
                5,
                "'I am no bird; and no net ensnares me.' A timeless anthem of soul and self-worth.")));

    list.add(
        book(
            48L,
            "The Phantom of the Opera",
            "Gaston Leroux",
            "Dark Romance",
            "Beneath the gilded balconies and velvet curtains of the Paris Opera House lies an underground lake inhabited by the disfigured musical genius known as the Opera Ghost. Obsessed with soprano Christine Daaé, the masked phantom tutors her into stardom while executing lethal vengeance against any who dare stand between them.",
            "9780141191508",
            4.5,
            410,
            1910,
            360,
            "English (translated from French)",
            List.of(
                "Paris Opera", "Obsessive Love", "Masked Phantom", "Gothic Romance"),
            false,
            91,
            review(
                "Christine Daaé",
                "June 19, 2025",
                5,
                "Macabre and tragic. The underground cellars and mirror passages are unforgettable."),
            review(
                "Raoul de Chagny",
                "August 31, 2025",
                4,
                "Erik is a terrifying monster who still manages to evoke unbearable pity."),
            review(
                "Madame Giry",
                "October 18, 2025",
                5,
                "The chandelier crash, the masquerade, and the red death—a decadent dark masterpiece.")));

    list.add(
        book(
            49L,
            "My Cousin Rachel",
            "Daphne du Maurier",
            "Dark Romance",
            "Young Philip Ashley is raised by his beloved cousin Ambrose in a magnificent Cornish estate until Ambrose dies suddenly in Italy shortly after marrying the mysterious Countess Rachel. When the radiant widow arrives in England, Philip intends to despise her, but finds himself helpless against her allure while questioning whether she is an angel or a poisoner.",
            "9781402217098",
            4.4,
            290,
            1951,
            352,
            "English",
            List.of(
                "Cornish Estate", "Femme Fatale", "Suspicion & Desire", "Psychological"),
            false,
            87,
            review(
                "Cornish Manor Watcher",
                "July 22, 2025",
                5,
                "Masterful ambiguity. Did Rachel poison Ambrose, or was Philip blinded by youth?"),
            review(
                "Anthony Vance",
                "September 14, 2025",
                4,
                "Du Maurier excels at the toxic intersection of erotic infatuation and mortal dread."),
            review(
                "Gwendolyn Marsh",
                "November 8, 2025",
                5,
                "A tense, elegant psychological romance where you never know who to trust.")));

    list.add(
        book(
            50L,
            "Haunting Adeline",
            "H. D. Carlton",
            "Dark Romance",
            "Author Adeline Reilly moves into her grandmother's gothic manor, only to discover she is being shadowed by a lethal vigilante known as Zade. As danger tightens around them and a sinister human trafficking syndicate targets Adeline, fear transforms into a dark, intoxicating surrender.",
            "9781501139385",
            4.4,
            520,
            2021,
            574,
            "English",
            List.of("Morally Grey", "Stalker Romance", "Gothic Mansion", "Dark Vigilante"),
            false,
            95,
            review(
                "Dark Fiction Reader",
                "August 10, 2025",
                4,
                "A dark, intense cat-and-mouse game with high tension and unapologetic thrills."),
            review(
                "Kendra Bell",
                "October 2, 2025",
                4,
                "The vigilante subplot adds adrenaline and the gothic house gives great atmosphere."),
            review(
                "Devon Thorne",
                "November 13, 2025",
                5,
                "Unflinchingly dark romance that kept me glued to the pages through the night.")));

    list.add(
        book(
            51L,
            "Corrupt",
            "Penelope Douglas",
            "Dark Romance",
            "Erika Fane has spent three years living in fear of the four dangerous young men she sent to prison on Devil's Night. Now Michael Crist and his vengeful brothers are free, and Michael has orchestrated a relentless cat-and-mouse game where Erika cannot run from the dark attraction connecting them.",
            "9781943893034",
            4.3,
            400,
            2015,
            442,
            "English",
            List.of(
                "Four Horsemen", "Revenge Romance", "High Stakes", "Enemies to Lovers"),
            false,
            89,
            review(
                "Devil's Night Fan",
                "June 15, 2025",
                4,
                "The tension on Devil's Night is palpable. Michael Crist is a ruthless, magnetic lead."),
            review(
                "Serena Blake",
                "August 28, 2025",
                4,
                "A thrilling, adrenaline-fueled enemies-to-lovers story packed with secrets."),
            review(
                "Brooke Davis",
                "October 27, 2025",
                5,
                "Dark, sexy, and utterly impossible to put down. Penelope Douglas writes elite angst.")));

    list.add(
        book(
            52L,
            "Credence",
            "Penelope Douglas",
            "Dark Romance",
            "After losing both parents, seventeen-year-old Tiernan de Haas is sent to the rugged Colorado mountains to reside with her estranged uncle and his two sons. Snowed in for winter in a cabin without electricity or rules, Tiernan sheds her emotional numbness and explores boundaries of devotion and instinct.",
            "9781777271404",
            4.2,
            430,
            2020,
            480,
            "English",
            List.of("Isolated Cabin", "Forbidden Love", "Wilderness Survival", "Taboo"),
            false,
            90,
            review(
                "Mountain Cabin Reader",
                "July 28, 2025",
                4,
                "The snowy Colorado isolation is an incredible backdrop for this taboo survival romance."),
            review(
                "Harley Quinn Fan",
                "September 18, 2025",
                4,
                "Intense, emotional, and boundary-pushing. Tiernan's healing is unexpectedly moving."),
            review(
                "Roxanne Miller",
                "November 5, 2025",
                4,
                "Atmospheric winter wilderness setting meets high emotional stakes.")));

    list.add(
        book(
            53L,
            "Hooked",
            "Emily McIntire",
            "Dark Romance",
            "James Barron has spent his entire existence planning revenge against Peter Michaels, the man who ruined his childhood and family. James targets Wendy, Peter's innocent daughter, using seduction as a weapon, only to find the young woman capturing the blackened heart of the villain.",
            "9781737544906",
            4.3,
            380,
            2021,
            334,
            "English",
            List.of(
                "Peter Pan Retelling", "Villain Romance", "Mob Revenge", "Dark Retelling"),
            false,
            88,
            review(
                "Neverland Dark Fan",
                "August 21, 2025",
                4,
                "The villain gets the girl! James as Captain Hook is lethal, broken, and protective."),
            review(
                "Wendy Darling Reader",
                "October 7, 2025",
                4,
                "Clever nods to the classic fairy tale wrapped in modern underworld crime."),
            review(
                "Maya Jenkins",
                "November 15, 2025",
                5,
                "Quick, spicy, and satisfying dark romance that redeems the misunderstood pirate.")));

    list.add(
        book(
            54L,
            "The Sweetest Oblivion",
            "Danielle Lori",
            "Dark Romance",
            "Known as the Abelli Mafia family's pristine princess, Elena Rossi has carefully maintained an obedient facade to preserve her family's honour. But when brutal Chicago boss Nicolas Russo is betrothed to her younger sister, his piercing gaze and dominating presence ignite an all-consuming rebellion.",
            "9781729497500",
            4.5,
            460,
            2018,
            384,
            "English",
            List.of(
                "Mafia Romance",
                "Arranged Marriage",
                "Enemies to Lovers",
                "Forbidden Attraction"),
            false,
            93,
            review(
                "Made Series Fan",
                "July 9, 2025",
                5,
                "The chemistry between Elena and Nicolas sizzles off the page. Best mafia romance out there."),
            review(
                "Gianna Abelli",
                "September 23, 2025",
                5,
                "Nicolas Russo is dangerous, witty, and unapologetic. The banter is perfection."),
            review(
                "Isabella Rossi",
                "October 31, 2025",
                4,
                "Exquisite tension and high-stakes underworld glamour. Top-tier dark romance.")));

    // =========================================================================
    // 7. SCIENCE FICTION (IDs 55 - 63)
    // =========================================================================
    list.add(
        book(
            55L,
            "Dune",
            "Frank Herbert",
            "Science Fiction",
            "Young Paul Atreides travels with his noble family to assume stewardship of Arrakis, a desolate desert planet that is the universe's only source of the mind-expanding spice melange. When a deadly betrayal by the rival Harkonnens shatters his house, Paul flees into the deep desert, rising as a prophesied messiah among the fierce Fremen tribes.",
            "9780441172719",
            4.8,
            650,
            1965,
            412,
            "English",
            List.of("Space Opera", "Arrakis", "Sandworms", "Chosen One"),
            true,
            99,
            review(
                "Fremen Sietch Leader",
                "June 18, 2025",
                5,
                "The greatest science fiction world ever created. Ecology, politics, and spice intertwined."),
            review(
                "Muad'Dib Follower",
                "August 24, 2025",
                5,
                "Herbert deconstructs the messiah myth with unmatched philosophical depth."),
            review(
                "Jessica Atreides Admirer",
                "October 16, 2025",
                5,
                "The Bene Gesserit litany against fear is poetry. A monumental achievement.")));

    list.add(
        book(
            56L,
            "The Left Hand of Darkness",
            "Ursula K. Le Guin",
            "Science Fiction",
            "Human envoy Genly Ai is dispatched to the frigid planet of Gethen to persuade its warring nations to join an interstellar alliance of worlds. Struggling to comprehend a culture where inhabitants possess no fixed gender, Genly forms an extraordinary bond of trust with a disgraced prime minister during a perilous trek across an ice sheet.",
            "9780441478125",
            4.6,
            340,
            1969,
            304,
            "English",
            List.of("Sociological Sci-Fi", "Winter Planet", "Ekumen", "Genderless Society"),
            false,
            89,
            review(
                "Estraven Admirer",
                "July 13, 2025",
                5,
                "Profound, philosophical, and visionary. The trek across the Gobrin Glacier is heartbreaking."),
            review(
                "Ekumen Scholar",
                "September 7, 2025",
                5,
                "Le Guin explores humanity, gender, and loneliness with supreme empathy."),
            review(
                "Thomas Sterling",
                "November 1, 2025",
                4,
                "A cornerstone of speculative fiction that challenges how we view society.")));

    list.add(
        book(
            57L,
            "Foundation",
            "Isaac Asimov",
            "Science Fiction",
            "Mathematician Hari Seldon develops psychohistory, an astonishing science capable of predicting the statistical behavior of human civilizations across millennia. Foreseeing the inevitable fall of the Galactic Empire, Seldon establishes the Foundation on the edge of the galaxy to preserve science and shorten the looming thirty-thousand-year dark age.",
            "9780553293357",
            4.7,
            480,
            1951,
            255,
            "English",
            List.of("Psychohistory", "Galactic Empire", "Hari Seldon", "Hard Sci-Fi"),
            false,
            93,
            review(
                "Hari Seldon Disciple",
                "May 29, 2025",
                5,
                "Psychohistory is one of the most brilliant concepts in the history of ideas."),
            review(
                "Galactic Trader",
                "August 14, 2025",
                4,
                "Ideas triumph over blasters. Asimov proves intellect is the greatest power in the stars."),
            review(
                "Arthur Cole",
                "October 23, 2025",
                5,
                "Grand sweeping historical perspective inspired by Rome's fall in outer space.")));

    list.add(
        book(
            58L,
            "The Martian",
            "Andy Weir",
            "Science Fiction",
            "After a vicious dust storm forces his crew to evacuate the Red Planet, astronaut Mark Watney is left behind, presumed dead with damaged equipment and meager rations. Armed with sharp wit, botanical ingenuity, and a refusal to surrender, Watney engineers his own survival on an inhospitable alien landscape.",
            "9780553418026",
            4.7,
            590,
            2011,
            384,
            "English",
            List.of("Hard Science", "Mars Survival", "Botany", "Space Engineering"),
            false,
            97,
            review(
                "Mark Watney Fan",
                "July 2, 2025",
                5,
                "Hilarious, optimistic, and intensely researched. Watney's voice makes math exhilarating."),
            review(
                "Ares 3 Crew",
                "September 19, 2025",
                5,
                "A love letter to scientific ingenuity and human determination in the face of despair."),
            review(
                "Laura Higgins",
                "November 8, 2025",
                4,
                "Gripping from the iconic opening sentence to the orbital rendezvous.")));

    list.add(
        book(
            59L,
            "Fahrenheit 451",
            "Ray Bradbury",
            "Science Fiction",
            "In a grim future where reading is outlawed and critical thought is suppressed by wall-sized television screens, fireman Guy Montag's job is to burn illicit books. After an encounter with an eccentric young neighbor awakens his conscience, Montag steals a hidden book and embarks on a dangerous quest to preserve humanity's literary heritage.",
            "9781451673319",
            4.6,
            520,
            1953,
            249,
            "English",
            List.of("Dystopian", "Book Burning", "Totalitarian Censorship", "Rebellion"),
            false,
            94,
            review(
                "Clarisse McClellan",
                "June 16, 2025",
                5,
                "Poetic prose warning against mindless entertainment and the erosion of literature."),
            review(
                "Professor Faber",
                "August 26, 2025",
                5,
                "Bradbury's lyrical style turns dystopia into prophetic, breathtaking art."),
            review(
                "David Sterling",
                "October 17, 2025",
                4,
                "The Mechanical Hound and the book-memorizing refugees in the woods stay with you.")));

    list.add(
        book(
            60L,
            "Neuromancer",
            "William Gibson",
            "Science Fiction",
            "Henry Case was one of the finest data-thieves in the Sprawl until vengeful employers crippled his nervous system with a mycotoxin, locking him out of the global matrix. Offered a surgical cure by an enigmatic patron, Case joins razor-girl Molly Millions to pull off an unprecedented hack against a godlike artificial intelligence.",
            "9780441569595",
            4.5,
            360,
            1984,
            271,
            "English",
            List.of("Cyberpunk", "Cyberspace", "AI Overlords", "Sprawl Trilogy"),
            false,
            88,
            review(
                "Molly Millions Club",
                "July 20, 2025",
                5,
                "'The sky above the port was the color of television, tuned to a dead channel.' Iconic."),
            review(
                "Console Cowboy",
                "September 11, 2025",
                4,
                "Invented cyberpunk aesthetics and cyberspace before the web even existed."),
            review(
                "Jax Ramirez",
                "October 29, 2025",
                4,
                "Gritty, neon-lit neon dystopia with unmatched atmosphere and sensory punch.")));

    list.add(
        book(
            61L,
            "Hyperion",
            "Dan Simmons",
            "Science Fiction",
            "On the eve of an apocalyptic galactic war, seven pilgrims embark on a final voyage to the mysterious Time Tombs on the planet Hyperion, guarded by the blade-covered creature known as the Shrike. Drawing inspiration from Chaucer, each pilgrim recounts their profound, heartbreaking story in hopes of unraveling the monster's secrets.",
            "9780553283686",
            4.7,
            390,
            1989,
            482,
            "English",
            List.of("Space Opera", "Time Tombs", "The Shrike", "Pilgrimage"),
            false,
            91,
            review(
                "The Scholar's Tale Admirer",
                "June 8, 2025",
                5,
                "Sol Weintraub's tale of his daughter aging backwards is one of the most moving in all fiction."),
            review(
                "Pilgrim of the Shrike",
                "August 18, 2025",
                5,
                "Massive ambition and literary scope. The worldbuilding across seven tales is staggering."),
            review(
                "Victor Stone",
                "November 5, 2025",
                4,
                "The Shrike is a horrifying, metallic terror. A true modern sci-fi epic.")));

    list.add(
        book(
            62L,
            "Ender's Game",
            "Orson Scott Card",
            "Science Fiction",
            "To defend Earth against an imminent invasion by an insectoid alien species, world militaries breed child prodigies and subject them to ruthless training in an orbital Battle School. Young Ender Wiggin excels in simulated war games with tactical brilliance, unaware of the devastating reality of his final examination.",
            "9780812550702",
            4.7,
            560,
            1985,
            324,
            "English",
            List.of(
                "Battle School", "Zero-G Combat", "Military Sci-Fi", "Tactical Genius"),
            false,
            95,
            review(
                "Battle School Veteran",
                "July 7, 2025",
                5,
                "The zero-gravity battleroom sequences and tactical genius are endlessly thrilling."),
            review(
                "Valentine Wiggin",
                "September 21, 2025",
                5,
                "The twist ending hits like a physical punch. A devastating study of child soldiers."),
            review(
                "Colonel Graff",
                "October 30, 2025",
                4,
                "Pacing is razor-sharp. Ender Wiggin's psychological burden is heartbreaking.")));

    list.add(
        book(
            63L,
            "Project Hail Mary",
            "Andy Weir",
            "Science Fiction",
            "Ryland Grace awakens from a coma aboard a spaceship with zero memory of who he is or why his two crewmates are dead. As his memories return, Grace realizes he is humanity's sole hope of stopping an interstellar pathogen from dimming the sun, aided by an unforgettable extraterrestrial ally.",
            "9780593135204",
            4.8,
            580,
            2021,
            496,
            "English",
            List.of(
                "First Contact", "Space Rescue", "Science Friendship", "Stellar Survival"),
            false,
            97,
            review(
                "Rocky's Best Friend",
                "May 24, 2025",
                5,
                "'Fist my bump!' The friendship between Grace and Rocky is the warmest thing in sci-fi."),
            review(
                "Astrophage Researcher",
                "August 11, 2025",
                5,
                "Pure joy from start to finish. Science, problem-solving, and cross-species kinship."),
            review(
                "Elena Rostov",
                "October 22, 2025",
                5,
                "Even better than The Martian. A triumphant, heartwarming interstellar adventure.")));

    // =========================================================================
    // 8. CONTEMPORARY FICTION (IDs 64 - 72)
    // =========================================================================
    list.add(
        book(
            64L,
            "Normal People",
            "Sally Rooney",
            "Contemporary Fiction",
            "In a small town in the west of Ireland, popular athletic Connell and proud, isolated Marianne pretend not to know each other at school, secretly sharing an intense emotional and physical bond. As both attend Trinity College in Dublin over the ensuing years, the shifting tides of power, trauma, and devotion test whether they can ever fully let go.",
            "9781984822185",
            4.4,
            510,
            2018,
            273,
            "English",
            List.of("Irish Fiction", "Coming of Age", "Intimacy", "Class Dynamics"),
            true,
            94,
            review(
                "Marianne & Connell",
                "July 16, 2025",
                5,
                "Rooney captures the unspoken awkwardness and profound ache of modern connection."),
            review(
                "Dublin Reader",
                "September 4, 2025",
                4,
                "Quietly devastating prose. The dialogue flows so naturally without quotation marks."),
            review(
                "Rachel Moore",
                "October 20, 2025",
                4,
                "A poignant, tender examination of how two people shape each other's entire lives.")));

    list.add(
        book(
            65L,
            "A Man Called Ove",
            "Fredrik Backman",
            "Contemporary Fiction",
            "Ove is an obstinate, curmudgeonly widower with strict neighborhood association rules, a short fuse, and an unwavering desire to end his solitary life and reunite with his late wife. But his plans are constantly derailed when a lively Iranian woman and her boisterous young family move in next door and accidentally run over his mailbox.",
            "9781476738024",
            4.8,
            580,
            2012,
            337,
            "English (translated from Swedish)",
            List.of(
                "Grumpy Heart of Gold",
                "Swedish Neighborhood",
                "Grief & Hope",
                "Community"),
            false,
            97,
            review(
                "Parvaneh Fan",
                "June 12, 2025",
                5,
                "You will laugh aloud on one page and sob on the next. Ove is a treasure."),
            review(
                "Saab Driver",
                "August 15, 2025",
                5,
                "A touching tribute to love, grief, and the life-saving magic of annoying neighbors."),
            review(
                "Hannah Larson",
                "November 1, 2025",
                5,
                "One of the warmest books I have ever read. Ove stole my whole heart.")));

    list.add(
        book(
            66L,
            "Eleanor Oliphant Is Completely Fine",
            "Gail Honeyman",
            "Contemporary Fiction",
            "Eleanor Oliphant lives a meticulously organized, solitary existence, avoiding unnecessary human interaction and nursing emotional scars with weekend vodka. When she and Raymond, the unkempt IT worker from her office, help an elderly man who has collapsed on the sidewalk, Eleanor's carefully constructed isolation begins to crack.",
            "9780735220690",
            4.6,
            530,
            2017,
            336,
            "English",
            List.of(
                "Social Isolation", "Trauma Recovery", "Quirky Voice", "Unlikely Friendship"),
            false,
            95,
            review(
                "Eleanor Oliphant Admirer",
                "July 24, 2025",
                5,
                "Eleanor's voice is unforgettable. Her gradual opening to human warmth is pure grace."),
            review(
                "Raymond from IT",
                "September 18, 2025",
                5,
                "Deals with severe childhood trauma with dignity, humor, and tender hope."),
            review(
                "Catherine Bell",
                "October 27, 2025",
                4,
                "Made me smile, cry, and want to reach out to every lonely soul in my town.")));

    list.add(
        book(
            67L,
            "Convenience Store Woman",
            "Sayaka Murata",
            "Contemporary Fiction",
            "Thirty-six-year-old Keiko Furukura has worked contentedly as a clerk in a Tokyo convenience store for eighteen years, finding peace in the predictable manuals, barcode beeps, and neatly organized shelves. Under intense pressure from family and friends to marry and secure an office career, Keiko takes radical steps to satisfy society's rigid norms.",
            "9780802128256",
            4.4,
            340,
            2016,
            163,
            "English (translated from Japanese)",
            List.of("Tokyo Society", "Conformity", "Neurodivergent", "Short Novel"),
            false,
            87,
            review(
                "Smile Mart Patron",
                "May 31, 2025",
                4,
                "A brilliant, deadpan critique of social conformity and corporate consumerism."),
            review(
                "Tokyo Reader",
                "August 7, 2025",
                5,
                "Short, punchy, and strangely triumphant. Keiko finds dignity on her own terms."),
            review(
                "Kenji Sato",
                "October 14, 2025",
                4,
                "The sounds and rhythms of the convenience store come alive vividly.")));

    list.add(
        book(
            68L,
            "Tomorrow, and Tomorrow, and Tomorrow",
            "Gabrielle Zevin",
            "Contemporary Fiction",
            "On a frigid December day in a Boston subway station, Harvard student Sam Masur spots his estranged childhood gaming companion Sadie Green among the crowd. Reunited by their shared passion for storytelling and code, they create an indie video game that turns into a global sensation, exploring thirty years of creative partnership, tragedy, and love.",
            "9780593321201",
            4.7,
            550,
            2022,
            416,
            "English",
            List.of(
                "Video Game Design", "Lifelong Friendship", "Creativity", "Boston & LA"),
            false,
            96,
            review(
                "Marx Watanabe Fan",
                "July 8, 2025",
                5,
                "The best novel about platonic creative partnership and art-making since Michael Chabon."),
            review(
                "Sadie & Sam Supporter",
                "September 22, 2025",
                5,
                "You don't need to play video games to fall completely in love with these characters."),
            review(
                "Lucas Grey",
                "November 9, 2025",
                4,
                "Exquisitely written. The 'Both Sides Now' chapter is devastating and beautiful.")));

    list.add(
        book(
            69L,
            "The Midnight Library",
            "Matt Haig",
            "Contemporary Fiction",
            "Between life and death lies the Midnight Library, a realm where shelves stretch into eternity and every book provides an opportunity to experience a life you might have lived had you made different choices. Nora Seed finds herself inside this magical sanctuary, traveling through alternate destinies to discover what truly makes existence worthwhile.",
            "9780525559474",
            4.5,
            570,
            2020,
            304,
            "English",
            List.of(
                "Parallel Lives", "Magical Realism", "Regrets & Choices", "Mental Health"),
            false,
            96,
            review(
                "Nora Seed Traveler",
                "June 29, 2025",
                5,
                "A gentle, comforting balm for anyone who has ever wrestled with regret."),
            review(
                "Mrs. Elm Disciple",
                "August 19, 2025",
                4,
                "Philosophical, empathetic, and full of cozy magical realism."),
            review(
                "Sophie Laurent",
                "October 31, 2025",
                5,
                "Reminds you that the simple, unglamorous life you have is worth living.")));

    list.add(
        book(
            70L,
            "Where the Crawdads Sing",
            "Delia Owens",
            "Contemporary Fiction",
            "For years, rumors of the 'Marsh Girl' have haunted the quiet coastal town of Barkley Cove, North Carolina. When handsome town golden boy Chase Andrews is found dead beneath a fire tower in the swamp, suspicious locals immediately turn their gaze upon barefoot recluse Kya Clark, who has raised herself wild among the gulls.",
            "9780735219090",
            4.6,
            600,
            2018,
            384,
            "English",
            List.of(
                "North Carolina Marsh", "Isolation", "Courtroom Drama", "Nature Lore"),
            false,
            98,
            review(
                "Kya Clark Defender",
                "July 18, 2025",
                5,
                "The descriptions of the North Carolina marsh wildlife are poetry. Kya is unforgettable."),
            review(
                "Tate Walker",
                "September 14, 2025",
                5,
                "Part coming-of-age ode to wilderness, part gripping courtroom drama."),
            review(
                "Miranda Shaw",
                "November 4, 2025",
                4,
                "An evocative story about resilience, nature's solace, and human cruelty.")));

    list.add(
        book(
            71L,
            "Anxious People",
            "Fredrik Backman",
            "Contemporary Fiction",
            "A failed bank robber bursts into an apartment open house and accidentally takes an eccentric ensemble of prospective buyers hostage. As the police surround the building, the captives share intimate secrets, irrational fears, and pizza, demonstrating how deeply broken people can rescue one another in moments of crisis.",
            "9781501160837",
            4.5,
            420,
            2019,
            352,
            "English (translated from Swedish)",
            List.of(
                "Hostage Drama", "Whimsical Humanism", "Apartment Open House", "Ensemble"),
            false,
            91,
            review(
                "Zara & Estelle Club",
                "August 12, 2025",
                5,
                "Backman has a superpower for finding the wounded, lovely humanity in absurd situations."),
            review(
                "Jack & Jim Police",
                "October 8, 2025",
                4,
                "Witty, compassionate, and surprisingly profound about adulthood and anxiety."),
            review(
                "Daniel Craig Fan",
                "November 12, 2025",
                5,
                "A warm hug in book form. The puzzle pieces fit together wonderfully.")));

    list.add(
        book(
            72L,
            "Little Fires Everywhere",
            "Celeste Ng",
            "Contemporary Fiction",
            "In the idyllic, meticulously planned suburb of Shaker Heights, Ohio, Elena Richardson upholds the rules of success and order. But when enigmatic nomadic artist Mia Warren and her teenage daughter rent a guesthouse from the Richardsons, a bitter community custody battle threatens to expose buried lies.",
            "9780735224292",
            4.5,
            490,
            2017,
            352,
            "English",
            List.of(
                "Suburban Secrets", "Shaker Heights", "Motherhood", "Moral Complexity"),
            false,
            93,
            review(
                "Pearl & Moody Reader",
                "July 3, 2025",
                5,
                "Ng dissects race, privilege, and motherhood in suburban America with surgical nuance."),
            review(
                "Mia Warren Art Admirer",
                "September 20, 2025",
                4,
                "Complex, morally ambiguous characters where nobody is entirely innocent or villainous."),
            review(
                "Audrey Miller",
                "October 25, 2025",
                5,
                "The slow burn of family tensions building toward the opening fire is masterly.")));

    // =========================================================================
    // 9. HISTORICAL FICTION (IDs 73 - 81)
    // =========================================================================
    list.add(
        book(
            73L,
            "The Book Thief",
            "Markus Zusak",
            "Historical Fiction",
            "Narrated by the sympathetic voice of Death, young Liesel Meminger is sent to live with a foster family on Himmel Street outside Munich during World War II. After finding a gravedigger's handbook in the snow, Liesel learns to read and steals forbidden volumes from Nazi burnings, sharing stories with her neighbors and the Jewish fist-fighter hidden in their basement.",
            "9780375842207",
            4.8,
            620,
            2005,
            552,
            "English",
            List.of(
                "WWII Germany", "Narrated by Death", "Power of Words", "Liesel Meminger"),
            true,
            98,
            review(
                "Death's Listener",
                "June 22, 2025",
                5,
                "Having Death narrate gives this monumental compassion and poetic heartbreak."),
            review(
                "Hans Hubermann Admirer",
                "August 17, 2025",
                5,
                "Hans playing the accordion for Liesel is etched in my soul forever."),
            review(
                "Max Vandenburg",
                "October 15, 2025",
                5,
                "A profound tribute to the life-saving magic of words and kindness in dark times.")));

    list.add(
        book(
            74L,
            "All the Light We Cannot See",
            "Anthony Doerr",
            "Historical Fiction",
            "In occupied France, blind French girl Marie-Laure flees Paris with her father to the walled citadel of Saint-Malo, carrying an invaluable museum jewel whispered to possess an ancient curse. Meanwhile, German orphan Werner Pfennig's genius for radio mechanics leads him into the brutal ranks of military intelligence, heading toward an unforgettable convergence.",
            "9781501173219",
            4.7,
            580,
            2014,
            531,
            "English",
            List.of(
                "Saint-Malo Siege", "Radio Waves", "Blind Heroine", "Pulitzer Winner"),
            false,
            97,
            review(
                "Marie-Laure Fan",
                "July 27, 2025",
                5,
                "Every sentence is a work of lapidary art. The miniature model of Paris is enchanting."),
            review(
                "Werner Pfennig Observer",
                "September 13, 2025",
                5,
                "A devastating, gorgeous Pulitzer winner about ethics, science, and moral choice."),
            review(
                "Etienne LeBlanc",
                "November 8, 2025",
                4,
                "Radio broadcasts bridging darkness across besieged wartime Saint-Malo.")));

    list.add(
        book(
            75L,
            "Wolf Hall",
            "Hilary Mantel",
            "Historical Fiction",
            "England in the 1520s teeters on the brink of disaster as King Henry VIII seeks to annul his marriage to Katherine of Aragon and wed Anne Boleyn without the Pope's consent. Thomas Cromwell, a blacksmith's abused son who has risen through intellect, bribery, and statecraft, navigates the lethal factions of the royal court to rewrite history.",
            "9780312429980",
            4.6,
            380,
            2009,
            653,
            "English",
            List.of("Tudor Court", "Thomas Cromwell", "Henry VIII", "Booker Prize"),
            false,
            92,
            review(
                "Tudor Court Historian",
                "August 9, 2025",
                5,
                "Mantel resurrects Cromwell with dazzling present-tense prose and ruthless wit."),
            review(
                "Cardinal Wolsey Supporter",
                "October 4, 2025",
                5,
                "The finest historical fiction of the twenty-first century. Politics as high art."),
            review(
                "Anne Boleyn Watcher",
                "November 10, 2025",
                4,
                "Immersive, demanding, and utterly rewarding portrait of power and intellect.")));

    list.add(
        book(
            76L,
            "The Nightingale",
            "Kristin Hannah",
            "Historical Fiction",
            "In tranquil France on the eve of German occupation, two estranged sisters face the realities of survival. While Vianne Mauriac must billet a Nazi officer in her home to protect her daughter, her rebellious younger sister Isabelle joins the French Resistance, risking execution to establish an escape line for shot-down Allied airmen across the Pyrenees.",
            "9781250080400",
            4.8,
            590,
            2015,
            440,
            "English",
            List.of(
                "French Resistance", "Two Sisters", "Occupied France", "Heartbreaking"),
            false,
            98,
            review(
                "Isabelle Rossignol",
                "June 14, 2025",
                5,
                "Both sisters show incredible heroism in totally different, harrowing ways."),
            review(
                "Vianne Mauriac Ally",
                "August 30, 2025",
                5,
                "Tears streamed down my face for the final hundred pages. Deeply moving tribute."),
            review(
                "Clara Fontaine",
                "October 28, 2025",
                5,
                "The courage of women during wartime brought to vivid, unforgettable life.")));

    list.add(
        book(
            77L,
            "Pachinko",
            "Min Jin Lee",
            "Historical Fiction",
            "In early 1900s Korea, young Sunja falls pregnant by a married yakuza boss, choosing to accept marriage to a sickly minister and emigrate to Osaka rather than bring dishonor upon her family. Spanning four generations of struggles, pachinko parlors, and cultural exile, this epic portrays the resilient heart of a family fighting for dignity.",
            "9781455563937",
            4.7,
            490,
            2017,
            496,
            "English",
            List.of(
                "Generational Saga", "Korean Immigrants", "Japan", "Family Sacrifice"),
            false,
            95,
            review(
                "Sunja's Grandchild",
                "July 19, 2025",
                5,
                "'History has failed us, but no matter.' A breathtaking multigenerational triumph."),
            review(
                "Hansu Observer",
                "September 25, 2025",
                5,
                "Deeply researched and emotionally gripping. Sunja's quiet strength is monumental."),
            review(
                "Kenji Takahashi",
                "November 3, 2025",
                4,
                "An essential, panoramic look at Zainichi Koreans across turbulent decades.")));

    list.add(
        book(
            78L,
            "The Seven Husbands of Evelyn Hugo",
            "Taylor Jenkins Reid",
            "Historical Fiction",
            "Aging Hollywood movie icon Evelyn Hugo is finally ready to tell the truth about her glamorous, scandalous existence and her seven headline-making marriages. Choosing obscure magazine reporter Monique Grant to write her authorized biography, Evelyn reveals ruthless ambition, concealed sacrifice, and the one true love she kept hidden from the spotlight.",
            "9781501161933",
            4.7,
            610,
            2017,
            400,
            "English",
            List.of(
                "Golden Age Hollywood", "Old Glamour", "Queer Romance", "Biopic"),
            false,
            98,
            review(
                "Celia St. James Club",
                "May 28, 2025",
                5,
                "Evelyn Hugo feels so vivid and real you cannot believe she was never an actual actress."),
            review(
                "Monique Grant Reader",
                "August 22, 2025",
                5,
                "Unapologetic, fierce, and heartbreakingly romantic. A modern classic."),
            review(
                "Harry Cameron Fan",
                "October 19, 2025",
                5,
                "The twists and emotional sacrifices kept me captivated until the last revelation.")));

    list.add(
        book(
            79L,
            "A Gentleman in Moscow",
            "Amor Towles",
            "Historical Fiction",
            "In 1922, an unrepentant aristocrat named Count Alexander Rostov is sentenced by a Bolshevik tribunal to spend the rest of his life inside the luxurious Hotel Metropol across from the Kremlin. As turbulent decades of Russian history unfold outside, Rostov discovers purpose, gastronomy, and enduring fatherhood within the grand hotel's walls.",
            "9780670026197",
            4.7,
            480,
            2016,
            462,
            "English",
            List.of(
                "Hotel Metropol", "Bolshevik Era", "Aristocratic Grace", "Witty"),
            false,
            94,
            review(
                "Count Rostov Fan",
                "June 19, 2025",
                5,
                "Gentlemanly grace, sublime culinary descriptions, and heartwarming charm."),
            review(
                "Nina Kulikova",
                "September 16, 2025",
                5,
                "The Metropol Hotel becomes an entire cosmos of dignity, music, and quiet rebellion."),
            review(
                "Pavel Andreyev",
                "November 11, 2025",
                4,
                "Towles's elegant wit turns house arrest into a profound celebration of life.")));

    list.add(
        book(
            80L,
            "The Pillars of the Earth",
            "Ken Follett",
            "Historical Fiction",
            "Set against the chaotic backdrop of twelfth-century civil war in England, ambitious prior Philip of Kingsbridge dreams of constructing the world's greatest Gothic cathedral. Master builder Tom, an outcast witch's daughter, and an aspiring mason unite their talents against sadistic nobles and treacherous bishops over forty years of stone and spirit.",
            "9780451166890",
            4.7,
            470,
            1989,
            973,
            "English",
            List.of(
                "Cathedral Building", "12th Century England", "The Anarchy", "Medieval Epic"),
            false,
            93,
            review(
                "Prior Philip Supporter",
                "July 12, 2025",
                5,
                "You become completely invested in cathedral architecture and village politics!"),
            review(
                "Jack Jackson Fan",
                "September 9, 2025",
                5,
                "Masterful storytelling of the grandest scale. You will feel every block of stone."),
            review(
                "Eileen Vance",
                "October 26, 2025",
                4,
                "An unforgettable historical epic that justifies every single one of its thousand pages.")));

    list.add(
        book(
            81L,
            "The Shadow of the Wind",
            "Carlos Ruiz Zafon",
            "Historical Fiction",
            "In post-civil war Barcelona, ten-year-old Daniel Sempere is brought by his antiquarian bookseller father to the secretive Cemetery of Forgotten Books, allowed to choose one volume to care for. Selecting a haunting novel by Julian Carax, Daniel soon discovers that someone has been systematically hunting down and burning every surviving copy of Carax's works.",
            "9780143034902",
            4.8,
            510,
            2001,
            487,
            "English (translated from Spanish)",
            List.of(
                "Cemetery of Forgotten Books", "Gothic Barcelona", "Mystery", "Book Love"),
            false,
            96,
            review(
                "Fermín Romero Fan",
                "August 3, 2025",
                5,
                "A love letter to the printed book, filled with misty Barcelona alleys and tragic romance."),
            review(
                "Julian Carax Reader",
                "October 1, 2025",
                5,
                "Fermín is the funniest, loyalest companion in modern fiction. Truly magical."),
            review(
                "Nuria Monfort",
                "November 13, 2025",
                5,
                "Gothic atmosphere, bibliophile mystery, and unforgettable melancholy beauty.")));

    // =========================================================================
    // 10. YOUNG ADULT (IDs 82 - 90)
    // =========================================================================
    list.add(
        book(
            82L,
            "The Hunger Games",
            "Suzanne Collins",
            "Young Adult",
            "In the ruins of North America, the tyrannical nation of Panem forces each of its twelve subjugated districts to send one boy and one girl to fight to the death in the televised Hunger Games. When sixteen-year-old Katniss Everdeen volunteers to take her younger sister's place, her sharp archery and fierce instinct spark a nationwide revolution.",
            "9780439023481",
            4.8,
            640,
            2008,
            374,
            "English",
            List.of("Dystopian Arena", "Katniss Everdeen", "Survival", "Rebellion"),
            true,
            99,
            review(
                "Mockingjay Rebel",
                "June 17, 2025",
                5,
                "Katniss Everdeen is one of the most formidable, real, and fierce protagonists in YA."),
            review(
                "Peeta Mellark Fan",
                "August 23, 2025",
                5,
                "The pacing is relentless, the social critique of reality TV and war is sharp."),
            review(
                "Rue's Lullaby",
                "October 17, 2025",
                5,
                "Heart-pounding from page one. A defining cultural touchstone of our generation.")));

    list.add(
        book(
            83L,
            "Six of Crows",
            "Leigh Bardugo",
            "Young Adult",
            "In the bustling, sinful international trade city of Ketterdam, criminal prodigy Kaz Brekker is offered a reward beyond his wildest dreams to pull off an impossible heist: infiltrate the impregnable Ice Court and rescue a hostage. Kaz assembles a crew of six lethal outcasts who must learn to trust one another if they wish to escape alive.",
            "9781250076960",
            4.8,
            580,
            2015,
            465,
            "English",
            List.of("Grishaverse Heist", "Ketterdam", "Found Family", "Kaz Brekker"),
            false,
            98,
            review(
                "Dregs Member",
                "July 25, 2025",
                5,
                "'No mourners, no funerals.' The found family dynamic and banter are unbeatable."),
            review(
                "Inej Ghafa Supporter",
                "September 17, 2025",
                5,
                "Kaz and Inej have the highest-stakes emotional tension in fantasy fiction."),
            review(
                "Jesper Fahey Fan",
                "November 6, 2025",
                5,
                "The heist plotting is Ocean's Eleven meets magical high-fantasy genius.")));

    list.add(
        book(
            84L,
            "The Cruel Prince",
            "Holly Black",
            "Young Adult",
            "Stolen from the mortal world as a child along with her sisters, Jude Duarte grows up in the dangerous, deceptive High Court of Faerie where mortals are despised and humiliated. Determined to claim her place among the fey nobility, Jude defies Prince Cardan and immerses herself in a deadly web of palace espionage.",
            "9780316310277",
            4.6,
            530,
            2018,
            370,
            "English",
            List.of("Faerie Court", "Mortal Ambition", "Prince Cardan", "Enemies to Lovers"),
            false,
            96,
            review(
                "Jude Duarte Loyalist",
                "May 22, 2025",
                5,
                "Jude is refreshingly bloodthirsty, calculating, and unapologetically ambitious."),
            review(
                "Cardan Greenbriar Club",
                "August 19, 2025",
                5,
                "Folk of the Air gives faerie lore back its dangerous, intoxicating teeth."),
            review(
                "Vivi Duarte",
                "October 24, 2025",
                4,
                "The political scheming in Elfhame is thrilling and Cardan's letters are iconic.")));

    list.add(
        book(
            85L,
            "A Wrinkle in Time",
            "Madeleine L'Engle",
            "Young Adult",
            "On a dark and stormy night, high school outcast Meg Murry receives an unearthly visit from Mrs. Whatsit, who reveals that a tesseract—a wrinkle in time and space—is real. Joined by her prodigy brother Charles Wallace and classmate Calvin, Meg travels across the cosmos to rescue her physicist father from the sinister disembodied brain known as IT.",
            "9780312367541",
            4.5,
            410,
            1962,
            211,
            "English",
            List.of(
                "Tesseract", "Cosmic Quest", "Meg Murry", "Classic Sci-Fi Fantasy"),
            false,
            89,
            review(
                "Mrs. Whatsit Friend",
                "June 29, 2025",
                5,
                "Meg's flaws—her anger and stubbornness—are precisely what allow her to save the day."),
            review(
                "Charles Wallace Fan",
                "September 1, 2025",
                4,
                "A visionary blend of theoretical physics, cosmic spirituality, and deep family love."),
            review(
                "Aunt Beast Guest",
                "October 29, 2025",
                4,
                "Pioneering YA sci-fi that has inspired generations of inquisitive readers.")));

    list.add(
        book(
            86L,
            "The Giver",
            "Lois Lowry",
            "Young Adult",
            "In a seemingly idyllic community with no crime, poverty, or emotional strife, twelve-year-old Jonas is selected to serve as the new Receiver of Memory. As an elder known as the Giver transfers genuine memories of color, joy, music, and suffering into Jonas's mind, the boy uncovers the horrific sacrifices required to maintain Sameness.",
            "9780544336261",
            4.6,
            490,
            1993,
            208,
            "English",
            List.of(
                "Dystopian Utopia", "Receiver of Memory", "Sameness", "Newbery Winner"),
            false,
            92,
            review(
                "Gabriel's Protector",
                "July 15, 2025",
                5,
                "The realization of what 'release' actually means chilled me to the marrow."),
            review(
                "Memory Keeper",
                "August 27, 2025",
                4,
                "Lowry makes you appreciate the colors, snow, music, and pain that define being human."),
            review(
                "Fiona's Friend",
                "November 2, 2025",
                5,
                "A spare, haunting masterpiece that questions the cost of false peace.")));

    list.add(
        book(
            87L,
            "Shadow and Bone",
            "Leigh Bardugo",
            "Young Adult",
            "Ravka is a kingdom torn in two by the Shadow Fold, a swath of impenetrable darkness crawling with winged monsters that feasts on human flesh. When humble cartographer orphan Alina Starkov unleashes an innate burst of radiant light to rescue her best friend, she is whisked away to the royal court as the legendary Sun Summoner.",
            "9781250027436",
            4.4,
            470,
            2012,
            358,
            "English",
            List.of(
                "Tsarist Fantasy", "Sun Summoner", "The Darkling", "Grishaverse"),
            false,
            91,
            review(
                "Little Palace Scholar",
                "May 19, 2025",
                4,
                "The Russian-inspired Tsarist aesthetic gives Ravka rich flavor and visual texture."),
            review(
                "Darkling Loyalist",
                "August 14, 2025",
                5,
                "The Darkling is a captivating villain. His line 'Fine, make me your villain' is immortal."),
            review(
                "Alina Starkov Supporter",
                "October 21, 2025",
                4,
                "Fun magic system, delicious court politics, and a fast-paced fantasy adventure.")));

    list.add(
        book(
            88L,
            "The Fault in Our Stars",
            "John Green",
            "Young Adult",
            "Sixteen-year-old Hazel Grace Lancaster, who lives with terminal thyroid cancer and an oxygen tank, attends a church youth support group where she meets Augustus Waters, an amputee in remission. Bonded by dark wit and a shared adoration for an elusive novel, the two embark on a life-changing journey to Amsterdam.",
            "9780525478812",
            4.7,
            600,
            2012,
            313,
            "English",
            List.of(
                "Contemporary YA", "Cancer Support Group", "Amsterdam Quest", "Bittersweet"),
            false,
            97,
            review(
                "Okay Okay Fan",
                "June 24, 2025",
                5,
                "'Some infinities are bigger than other infinities.' Witty, devastating, and genuine."),
            review(
                "Gus Waters Club",
                "September 12, 2025",
                5,
                "Hazel and Augustus refuse to let illness strip them of intellect, wit, and dignity."),
            review(
                "Isaac Support",
                "November 7, 2025",
                4,
                "Bring tissues. A landmark contemporary novel that treats teenage feelings seriously.")));

    list.add(
        book(
            89L,
            "Percy Jackson & the Olympians: The Lightning Thief",
            "Rick Riordan",
            "Young Adult",
            "Twelve-year-old Percy Jackson discovers his dyslexia and ADHD are actually battle reflexes when his pre-algebra teacher transforms into a mythical fury and attacks him. Rushed to Camp Half-Blood for demigod children, Percy learns he is the son of Poseidon and must retrieve Zeus's stolen master lightning bolt before an Olympian war ignites.",
            "9780786838653",
            4.8,
            620,
            2005,
            377,
            "English",
            List.of(
                "Greek Mythology", "Camp Half-Blood", "Demigod", "Zeus Master Bolt"),
            false,
            98,
            review(
                "Cabin Three Camper",
                "July 10, 2025",
                5,
                "Percy's voice is hilarious, sarcastic, and endlessly lovable. Mythological perfection."),
            review(
                "Annabeth Chase Fan",
                "August 31, 2025",
                5,
                "Making ADHD and dyslexia demigod superpowers inspired an entire generation of kids."),
            review(
                "Grover Underwood",
                "October 25, 2025",
                5,
                "Action-packed, funny, and clever modernization of classic Greek myths.")));

    list.add(
        book(
            90L,
            "Children of Blood and Bone",
            "Tomi Adeyemi",
            "Young Adult",
            "In the mythical realm of Orïsha, a ruthless king ordered the slaughter of all maji, leaving young Zélie Adebola without a mother and her people without hope or magic. When an unexpected opportunity arises to strike back against the monarchy, Zélie allies with a runaway princess to outrun a vengeful crown prince and restore the spirits of magic.",
            "9781250170972",
            4.5,
            420,
            2018,
            544,
            "English",
            List.of(
                "West African Fantasy", "Magic Awakening", "Orïsha", "Rebellion"),
            false,
            92,
            review(
                "Zélie Adebola Supporter",
                "June 11, 2025",
                5,
                "West African mythology meets epic quest fantasy. High octane, vivid, and powerful."),
            review(
                "Amari Princess",
                "September 19, 2025",
                4,
                "The stakes feel monumental and the grief of cultural erasure is palpably raw."),
            review(
                "Inan Watcher",
                "November 14, 2025",
                4,
                "Rich Yoruba-inspired magic and dynamic characters who fight for their people.")));

    // =========================================================================
    // 11. GOTHIC FICTION (IDs 91 - 99)
    // =========================================================================
    list.add(
        book(
            91L,
            "Rebecca",
            "Daphne du Maurier",
            "Gothic Fiction",
            "An insecure companion marries the wealthy widower Maxim de Winter and arrives at Manderley, his vast and breathtaking Cornish estate. But she quickly discovers that the memory of his first wife Rebecca still lingers over every carpet, flower vase, and the sinister housekeeper Mrs. Danvers.",
            "9780380730407",
            4.8,
            560,
            1938,
            416,
            "English",
            List.of("Manderley", "Gothic Romance", "Mrs. Danvers", "Haunted Memory"),
            true,
            98,
            review(
                "Manderley Dreamer",
                "July 21, 2025",
                5,
                "'Last night I dreamt I went to Manderley again.' Atmospheric perfection from start to finish."),
            review(
                "Mrs. Danvers Watcher",
                "September 15, 2025",
                5,
                "The sheer dread in the morning room and the bedroom window scene is chilling."),
            review(
                "Maxim de Winter Confidant",
                "October 28, 2025",
                5,
                "A masterwork of psychological gothic suspense that never dates or loses its power.")));

    list.add(
        book(
            92L,
            "The Picture of Dorian Gray",
            "Oscar Wilde",
            "Gothic Fiction",
            "Enthralled by Lord Henry Wotton's hedonistic worldview, the breathtakingly handsome Dorian Gray makes a wish that his painted portrait will bear the ravages of age and sin while his physical youth remains untouched. As Dorian plunges into secret debauchery, the rotting painting in his locked attic mirrors the decay of his soul.",
            "9780141439570",
            4.7,
            540,
            1890,
            254,
            "English",
            List.of(
                "Aestheticism",
                "Faustian Bargain",
                "Secret Portrait",
                "Victorian Decadence"),
            false,
            97,
            review(
                "Basil Hallward",
                "August 18, 2025",
                5,
                "Wilde's epigrams sparkle like poisoned diamonds. A stunning aesthetic cautionary tale."),
            review(
                "Lord Henry Disciple",
                "October 6, 2025",
                5,
                "The psychological terror of the locked attic and the rotting canvas is unforgettable."),
            review(
                "Sibyl Vane Mourner",
                "November 10, 2025",
                4,
                "Wilde's only novel is a philosophical masterpiece of vanity, guilt, and artistic soul.")));

    list.add(
        book(
            93L,
            "Carmilla",
            "Sheridan Le Fanu",
            "Gothic Fiction",
            "Isolated in an ancient Austrian castle in Styria, young Laura longs for companionship until a carriage accident brings the enigmatic, aristocratic Carmilla into her home as a recuperating guest. Laura is enchanted by Carmilla's strange, passionate affection, even as horrific night fevers plague the village maidens.",
            "9781843910297",
            4.5,
            320,
            1872,
            108,
            "English",
            List.of("Vampire Classic", "Styrian Castle", "Sensual Gothic", "Pre-Dracula"),
            false,
            88,
            review(
                "Styrian Castle Guest",
                "June 25, 2025",
                5,
                "Predates Dracula by a quarter-century and remains more sensual, poetic, and intimate."),
            review(
                "Laura of Karnstein",
                "August 28, 2025",
                4,
                "The atmosphere of the crumbling Austrian schloss and pine forests is mesmerizing."),
            review(
                "General Spieldorf",
                "October 19, 2025",
                5,
                "A foundational vampire novella with hypnotic prose and dreamy gothic menace.")));

    list.add(
        book(
            94L,
            "The Fall of the House of Usher",
            "Edgar Allan Poe",
            "Gothic Fiction",
            "An unnamed narrator journeys on horseback to the crumbling, melancholy ancestral mansion of his childhood friend Roderick Usher, whose physical and mental faculties are deteriorating under an inherited sensory curse. When Roderick's twin sister Madeline dies and is entombed in the vaults below, the estate begins to fracture.",
            "9780141398181",
            4.6,
            410,
            1839,
            144,
            "English",
            List.of(
                "Southern Gothic", "Roderick Usher", "Tarn", "Morbid Melancholy"),
            false,
            91,
            review(
                "Roderick Usher Friend",
                "July 14, 2025",
                5,
                "Poe's architecture mirrors the decaying mind. The fissure in the wall is pure symbolism."),
            review(
                "Madeline Usher Mourner",
                "September 23, 2025",
                5,
                "The dark tarn, the decaying trees, the tapestries—gothic atmosphere incarnate."),
            review(
                "Raven Reader",
                "November 5, 2025",
                4,
                "Short, terrifying, and rhythmic. Poe at the absolute height of his macabre craft.")));

    list.add(
        book(
            95L,
            "The Castle of Otranto",
            "Horace Walpole",
            "Gothic Fiction",
            "Recognized as the foundational progenitor of all Gothic literature, this macabre tale begins when Manfred's young son is crushed to death on his wedding day by a colossal supernatural helmet falling from the heavens. Terrified of an ancient prophecy foretelling his family's doom, Manfred seeks to divorce his wife and marry his son's intended bride.",
            "9780199537273",
            4.1,
            190,
            1764,
            125,
            "English",
            List.of(
                "First Gothic Novel",
                "Giant Helmet",
                "Ancestral Curse",
                "Castle Subterranean"),
            false,
            76,
            review(
                "Gothic Antiquarian",
                "May 12, 2025",
                4,
                "The novel that started it all! Trapdoors, ancestral portraits, and giant armor."),
            review(
                "Lord Manfred Watcher",
                "August 16, 2025",
                4,
                "Melodramatic historical curiosity that created the entire gothic playbook."),
            review(
                "Isabella of Vicenza",
                "October 22, 2025",
                4,
                "Essential reading for anyone studying the origins of dark literary romance.")));

    list.add(
        book(
            96L,
            "We Have Always Lived in the Castle",
            "Shirley Jackson",
            "Gothic Fiction",
            "Eighteen-year-old Mary Katherine 'Merricat' Blackwood resides in secluded isolation with her elder sister Constance and ailing Uncle Julian on their Vermont estate. Six years after the rest of their family died from arsenic sprinkled in the sugar bowl, an estranged cousin arrives seeking the Blackwood fortune.",
            "9780143039761",
            4.7,
            430,
            1962,
            160,
            "English",
            List.of(
                "Merricat Blackwood", "Arsenic Sugar Bowl", "Unreliable Narrator", "Poison"),
            false,
            93,
            review(
                "Merricat Blackwood Fan",
                "July 2, 2025",
                5,
                "Merricat is my favorite narrator in fiction. Witchy, fierce, and delightfully unhinged."),
            review(
                "Constance Blackwood",
                "September 11, 2025",
                5,
                "A quiet masterpiece about sisterhood, village cruelty, and delicious homemade preserves."),
            review(
                "Uncle Julian Memory",
                "October 30, 2025",
                4,
                "Jackson creates unbearable psychological tension out of a tea service and buried marbles.")));

    list.add(
        book(
            97L,
            "The Monk",
            "Matthew Gregory Lewis",
            "Gothic Fiction",
            "In Madrid, Father Ambrosio is celebrated as a paragon of holy piety whose fiery sermons pack churches with adoring parishioners. Seduced by a novice who harbors demonic secrets, Ambrosio descends into a maelstrom of lust, sorcery, incest, and murder, culminating in a terrifying reckoning with the Devil.",
            "9780140436037",
            4.3,
            220,
            1796,
            368,
            "English",
            List.of(
                "Dark Gothic", "Capuchin Monk", "Demonic Temptation", "Scandalous Classic"),
            false,
            80,
            review(
                "Spanish Inquisitor",
                "June 20, 2025",
                4,
                "Written when Lewis was twenty, this scandalous gothic shocker remains outrageous fun."),
            review(
                "Rosario Admirer",
                "August 27, 2025",
                4,
                "Demons, subterranean vaults, and bleeding nuns. High gothic excess at its peak."),
            review(
                "Don Lorenzo",
                "November 9, 2025",
                5,
                "An electrifying, lurid page-turner that scandalized eighteenth-century England.")));

    list.add(
        book(
            98L,
            "The Thirteenth Tale",
            "Diane Setterfield",
            "Gothic Fiction",
            "Reclusive, terminally ill author Vida Winter has made a career of inventing fanciful lies about her mysterious childhood, but now she invites amateur biographer Margaret Lea to write her true story. Traveling to Angelfield, a ruined Yorkshire mansion, Margaret uncovers the dark, forgotten secrets of the March family and the tragic fate of twin sisters.",
            "9780743298032",
            4.6,
            380,
            2006,
            406,
            "English",
            List.of(
                "Modern Gothic", "Yorkshire Estate", "Twin Mystery", "Bibliophile Mystery"),
            false,
            90,
            review(
                "Vida Winter Reader",
                "July 29, 2025",
                5,
                "A lush, modern homage to Jane Eyre and the Brontës. The writing is pure velvet."),
            review(
                "Margaret Lea Ally",
                "September 20, 2025",
                5,
                "The ruined March estate and the ghost-like twins make this atmospheric bliss."),
            review(
                "Angelfield Ghost",
                "October 27, 2025",
                4,
                "A brilliant, twisty gothic puzzle celebrating storytelling and family secrets.")));

    list.add(
        book(
            99L,
            "The Woman in Black",
            "Susan Hill",
            "Gothic Fiction",
            "Young solicitor Arthur Kipps is sent to the foggy English coastal village of Crythin Gifford to attend the funeral and settle the estate of the eccentric, deceased Alice Drablow. Marooned at Eel Marsh House when the ocean tide cuts off the causeway, Arthur encounters the malevolent spectre of a wasted woman dressed in black.",
            "9780307745316",
            4.5,
            370,
            1983,
            160,
            "English",
            List.of(
                "Eel Marsh House", "Ghost Story", "Causeway Mist", "Vengeful Specter"),
            false,
            89,
            review(
                "Arthur Kipps Client",
                "August 11, 2025",
                5,
                "The rising sea tide cutting off the causeway is the definition of claustrophobic dread."),
            review(
                "Crythin Gifford Resident",
                "October 13, 2025",
                4,
                "Classic Victorian ghost storytelling perfected in modern times. Hair-raising."),
            review(
                "Spider the Dog Fan",
                "November 15, 2025",
                5,
                "The rocking chair in the nursery gave me absolute chills. Wonderfully terrifying.")));

    // =========================================================================
    // 12. PSYCHOLOGICAL THRILLER (IDs 100 - 108)
    // =========================================================================
    list.add(
        book(
            100L,
            "The Silent Patient",
            "Alex Michaelides",
            "Psychological Thriller",
            "Famous painter Alicia Berenson lives an enviable life with her fashion-photographer husband in a grand London home until she shoots him five times in the face and never utters another word. Criminal psychotherapist Theo Faber becomes obsessed with uncovering her motive and eliciting her speech in an experimental psychiatric facility.",
            "9781250301697",
            4.7,
            610,
            2019,
            336,
            "English",
            List.of(
                "Psychiatric Clinic", "Mute Painter", "Greek Tragedy", "Shocking Twist"),
            true,
            98,
            review(
                "Theo Faber Colleague",
                "June 23, 2025",
                5,
                "The Greek tragedy framework of Alcestis is brilliant. The twist left my jaw on the floor."),
            review(
                "The Grove Clinic Staff",
                "August 30, 2025",
                5,
                "Clean, gripping pacing with deep psychological probing into trauma and silence."),
            review(
                "Alicia's Diary Reader",
                "October 18, 2025",
                4,
                "A phenomenal page-turner that plays totally fair while concealing its true trick.")));

    list.add(
        book(
            101L,
            "Verity",
            "Colleen Hoover",
            "Psychological Thriller",
            "Struggling writer Lowen Ashleigh is hired by grieving husband Jeremy Crawford to finish the remaining books of a successful series started by his injured wife, Verity. Sorting through Verity's chaotic home office, Lowen uncovers an unpublished, bone-chilling autobiographical manuscript admitting to sinister family crimes.",
            "9781982183780",
            4.6,
            630,
            2018,
            336,
            "English",
            List.of(
                "Twisted Manuscript",
                "Injured Author",
                "Dark Romance Thriller",
                "Unreliable"),
            false,
            98,
            review(
                "Team Manuscript",
                "July 19, 2025",
                5,
                "Unsettling, twisted, and totally unhinged. The manuscript excerpts will creep you out."),
            review(
                "Team Letter",
                "September 14, 2025",
                5,
                "The final letter debate will divide book clubs forever. Morbidly addictive."),
            review(
                "Lowen Ashleigh Fan",
                "November 4, 2025",
                4,
                "A dark, twisted psychological thrill-ride that leaves you questioning every character.")));

    list.add(
        book(
            102L,
            "The Girl on the Train",
            "Paula Hawkins",
            "Psychological Thriller",
            "Heartbroken and alcoholic Rachel Watson commutes into London each day, gazing out the train window at a row of Victorian homes and fantasizing about the perfect couple she sees on their deck. When she glimpses something shocking seconds before blacking out, Rachel wakes with bruises and finds the woman has disappeared.",
            "9781594634024",
            4.4,
            570,
            2015,
            336,
            "English",
            List.of(
                "Commuter Train", "Blackout Alcoholic", "Missing Woman", "Multiple POVs"),
            false,
            95,
            review(
                "London Commuter",
                "June 27, 2025",
                4,
                "Rachel's alcoholic fog makes her the most unreliable, vulnerable narrator in years."),
            review(
                "Megan Hipwell Watcher",
                "August 19, 2025",
                5,
                "The three women's overlapping points of view ratchet up tension to a fever pitch."),
            review(
                "Anna Watson Supporter",
                "October 23, 2025",
                4,
                "Claustrophobic, realistic, and keeps you guessing the killer until the train tracks.")));

    list.add(
        book(
            103L,
            "Behind Her Eyes",
            "Sarah Pinborough",
            "Psychological Thriller",
            "Single mother Louise begins an affair with her charming new psychiatrist boss, David, while simultaneously developing a secretive, supportive friendship with his elegant, isolated wife Adele. As Louise becomes hopelessly entangled in their marriage, she realizes Adele's diaries teach terrifying secrets about nocturnal dreaming.",
            "9781250111173",
            4.5,
            410,
            2017,
            384,
            "English",
            List.of("Love Triangle", "Astral Projection", "Lucid Dreaming", "WTF Ending"),
            false,
            91,
            review(
                "Adele's Diary Reader",
                "July 11, 2025",
                5,
                "#WTFthatending was trending for a reason. The final twist is utterly audacious."),
            review(
                "David's Patient",
                "September 26, 2025",
                4,
                "Cleverly weaves domestic infidelity with uncanny lucidity and nocturnal dread."),
            review(
                "Louise's Friend",
                "November 1, 2025",
                5,
                "You think you know where this triangle is heading, and then the trapdoors drop.")));

    list.add(
        book(
            104L,
            "Misery",
            "Stephen King",
            "Psychological Thriller",
            "Bestselling author Paul Sheldon crashes his car in a remote Colorado blizzard after completing a manuscript killing off his long-running historical romance heroine Misery Chastain. Rescued by Annie Wilkes, a former nurse who declares herself his 'number one fan,' Paul wakes to find himself imprisoned and forced to resurrect the character under pain of torture.",
            "9781501156748",
            4.8,
            580,
            1987,
            310,
            "English",
            List.of(
                "Obsessed Fan", "Annie Wilkes", "Colorado Blizzard", "Claustrophobia"),
            false,
            97,
            review(
                "Paul Sheldon Reader",
                "May 17, 2025",
                5,
                "Annie Wilkes is Stephen King's scariest monster because she could be totally real."),
            review(
                "Royal Typewriter Fan",
                "August 22, 2025",
                5,
                "A masterclass in single-room claustrophobia and the terrifying demands of fandom."),
            review(
                "Sidney Sheldon Fan",
                "October 14, 2025",
                5,
                "The thumb and axe scenes are harrowing. Pure psychological torture and survival.")));

    list.add(
        book(
            105L,
            "The Push",
            "Ashley Audrain",
            "Psychological Thriller",
            "Blythe Connor is determined to be the warm, devoted mother she never had, but immediately senses something chillingly wrong with her firstborn daughter Violet, who is cold and calculating. As her husband dismisses her fears as postpartum paranoia, a playground catastrophe shatters the family.",
            "9781984881663",
            4.5,
            390,
            2021,
            320,
            "English",
            List.of(
                "Maternal Dread", "Family Inheritance", "Disturbed Child", "Gaslighting"),
            false,
            90,
            review(
                "Blythe Connor Supporter",
                "June 30, 2025",
                5,
                "Unflinchingly explores maternal ambivalence and generational trauma. Deeply unsettling."),
            review(
                "Fox & Violet Observer",
                "September 8, 2025",
                4,
                "The dread tightens like a vise in every chapter. That final sentence will leave you gasping."),
            review(
                "Samira Bell",
                "November 10, 2025",
                5,
                "Audrain captures the horror of being gaslit by your own spouse with devastating realism.")));

    list.add(
        book(
            106L,
            "Sharp Objects",
            "Gillian Flynn",
            "Psychological Thriller",
            "Fresh from a brief stay at a psych hospital for self-harm, Chicago journalist Camille Preaker is assigned to cover the gruesome unsolved murders of two young girls in her hometown of Wind Gap, Missouri. Staying in her neurotic mother's Victorian mansion, Camille must confront her estranged family and a toxic southern community.",
            "9780307341556",
            4.6,
            460,
            2006,
            254,
            "English",
            List.of(
                "Southern Gothic Thriller",
                "Camille Preaker",
                "Self-Harm",
                "Family Poison"),
            false,
            93,
            review(
                "Wind Gap Resident",
                "July 22, 2025",
                5,
                "Southern gothic noir drenched in humidity, toxic motherhood, and dollhouse dread."),
            review(
                "Amma Crellin Watcher",
                "September 17, 2025",
                5,
                "Flynn's debut novel is even darker and more intimate than Gone Girl."),
            review(
                "Richard Willis",
                "October 26, 2025",
                4,
                "The ivory floor of the dollhouse... That revelation gave me nightmares.")));

    list.add(
        book(
            107L,
            "Sometimes I Lie",
            "Alice Feeney",
            "Psychological Thriller",
            "Amber Reynolds is in a coma, unable to move or open her eyes, yet she can hear everything happening around her hospital bed. Switching between her current paralysis, the days leading up to the accident, and childhood diary entries from twenty years earlier, Amber tries to piece together whether her husband caused her condition.",
            "9781250144843",
            4.4,
            340,
            2017,
            288,
            "English",
            List.of(
                "Coma Patient", "Locked-In Syndrome", "Childhood Diaries", "Mind Games"),
            false,
            87,
            review(
                "Amber Reynolds Fan",
                "August 14, 2025",
                4,
                "'My name is Amber Reynolds. There are three things you should know about me: 1. I'm in a coma. 2. My husband doesn't love me anymore. 3. Sometimes I lie.' Hooked immediately!"),
            review(
                "Paul Reynolds Watcher",
                "October 4, 2025",
                4,
                "Diabolically clever plotting with twists layered on top of twists."),
            review(
                "Claire Vance",
                "November 12, 2025",
                5,
                "Fast, twisty, and deliciously untrustworthy narration from beginning to end.")));

    list.add(
        book(
            108L,
            "The Guest List",
            "Lucy Foley",
            "Psychological Thriller",
            "On an eerie, windswept island off the coast of Ireland, guests assemble for the celebrity wedding of rising television star Will Slater and digital magazine publisher Jules Keegan. As Champagne flows and resentment boils, a howling storm cuts power to the island, and a server is discovered dead outside in the bog.",
            "9780062868930",
            4.4,
            450,
            2020,
            336,
            "English",
            List.of(
                "Island Wedding", "Irish Coast", "Whodunit Thriller", "Ensemble Suspense"),
            false,
            92,
            review(
                "Inis an Amplóra Guest",
                "June 18, 2025",
                4,
                "And Then There Were None meets Big Little Lies on a stormy Connemara island."),
            review(
                "Aoife the Planner",
                "August 29, 2025",
                5,
                "Everyone has a motive and every guest is hiding a vicious secret."),
            review(
                "Hannah Foley Fan",
                "October 20, 2025",
                4,
                "The howling wind and peat bog setting create magnificent, tense atmosphere.")));

    ALL_BOOKS.addAll(list);
    for (BookItem b : list) {
      BOOKS_BY_ID.put(b.getId(), b);
    }
    loadCustomBooksFromDatabase();
  }

  /** Dynamically registers a newly added custom book into memory so it appears across discovery and catalogue searches. */
  public static void addCustomBook(BookItem item) {
    if (item == null) return;
    BOOKS_BY_ID.put(item.getId(), item);
    ALL_BOOKS.removeIf(b -> b.getId() == item.getId());
    ALL_BOOKS.add(item);
  }

  /** Loads all custom user-added books (id > 108) from MySQL books table into memory if available. */
  public static void loadCustomBooksFromDatabase() {
    try {
      var rows = Sql.query("SELECT b.*, g.name AS genre_name FROM books b JOIN genres g ON b.genre_id=g.id WHERE b.id > 108");
      for (var row : rows) {
        long id = ((Number) row.get("id")).longValue();
        if (!BOOKS_BY_ID.containsKey(id)) {
          String title = String.valueOf(row.get("title"));
          String author = String.valueOf(row.get("author"));
          String genre = String.valueOf(row.get("genre_name"));
          String description = String.valueOf(row.get("description"));
          String cover = row.get("cover_image_url") != null ? String.valueOf(row.get("cover_image_url")) : "";
          String isbn = row.get("isbn") != null ? String.valueOf(row.get("isbn")) : "";
          int year = row.get("publication_year") != null ? ((Number) row.get("publication_year")).intValue() : 0;
          int pages = row.get("total_pages") != null ? ((Number) row.get("total_pages")).intValue() : 0;
          BookItem b =
              new BookItem(
                  id,
                  title,
                  author,
                  genre,
                  description,
                  cover,
                  isbn,
                  0.0,
                  0,
                  year,
                  pages,
                  "English",
                  List.of(genre),
                  false,
                  1,
                  Collections.emptyList());
          addCustomBook(b);
        }
      }
    } catch (Throwable ignored) {
    }
  }

  private BookCatalogue() {}

  /** Helper to construct a BookItem cleanly. */
  private static BookItem book(
      long id,
      String title,
      String author,
      String genre,
      String description,
      String isbn,
      double rating,
      int reviewCount,
      int year,
      int pages,
      String language,
      List<String> tags,
      boolean featured,
      int popularity,
      ReviewItem r1,
      ReviewItem r2,
      ReviewItem r3) {
    String coverUrl = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
    return new BookItem(
        id,
        title,
        author,
        genre,
        description,
        coverUrl,
        isbn,
        rating,
        reviewCount,
        year,
        pages,
        language,
        tags,
        featured,
        popularity,
        List.of(r1, r2, r3));
  }

  /** Helper to construct a sample review item. */
  private static ReviewItem review(String name, String date, int rating, String text) {
    return new ReviewItem(name, date, rating, text);
  }

  /** Returns all 108 books in the catalogue. */
  public static List<BookItem> getAll() {
    return ALL_BOOKS;
  }

  /** Returns all 108 books as JSP-compatible Maps. */
  public static List<Map<String, Object>> getAllAsMaps() {
    return ALL_BOOKS.stream().map(BookItem::toMap).collect(Collectors.toList());
  }

  /** Finds a book by its unique ID. */
  public static Optional<BookItem> getById(long id) {
    return Optional.ofNullable(BOOKS_BY_ID.get(id));
  }

  /** Returns all genres available in the catalogue. */
  public static List<String> getGenres() {
    return GENRES;
  }

  /**
   * Performs in-memory filtering and sorting across the 108 books.
   *
   * @param query search text matching title, author, genre, tags, or description
   * @param genre filter by genre (null or empty for all)
   * @param sort sort mode: 'recommended', 'rating', 'reviews', 'title', 'year', 'author', 'popular'
   * @param page 1-indexed page number (0 to return all results without pagination)
   * @param pageSize number of items per page
   * @return list of matching BookItems
   */
  public static List<BookItem> search(
      String query, String genre, String sort, int page, int pageSize) {
    String q = (query == null) ? "" : query.trim().toLowerCase();
    String g = (genre == null) ? "" : genre.trim();

    List<BookItem> matched =
        ALL_BOOKS.stream()
            .filter(
                b -> {
                  if (!g.isEmpty() && !b.getGenre().equalsIgnoreCase(g)) {
                    return false;
                  }
                  if (q.isEmpty()) return true;
                  if (b.getTitle().toLowerCase().contains(q)) return true;
                  if (b.getAuthor().toLowerCase().contains(q)) return true;
                  if (b.getGenre().toLowerCase().contains(q)) return true;
                  for (String tag : b.getTags()) {
                    if (tag.toLowerCase().contains(q)) return true;
                  }
                  return b.getDescription().toLowerCase().contains(q);
                })
            .collect(Collectors.toCollection(ArrayList::new));

    Comparator<BookItem> comparator =
        switch (sort == null ? "" : sort.toLowerCase()) {
          case "rating" ->
              Comparator.comparingDouble(BookItem::getRating)
                  .reversed()
                  .thenComparing(BookItem::getReviewCount, Comparator.reverseOrder());
          case "reviews", "popular" ->
              Comparator.comparingInt(BookItem::getReviewCount)
                  .reversed()
                  .thenComparingDouble(BookItem::getRating)
                  .reversed();
          case "author" -> Comparator.comparing(BookItem::getAuthor);
          case "year" -> Comparator.comparingInt(BookItem::getPublicationYear).reversed();
          case "title" -> Comparator.comparing(BookItem::getTitle);
          case "recommended" ->
              Comparator.comparing((BookItem b) -> b.isFeatured() ? 0 : 1)
                  .thenComparing(BookItem::getPopularity, Comparator.reverseOrder())
                  .thenComparingDouble(BookItem::getRating)
                  .reversed();
          default ->
              Comparator.comparing((BookItem b) -> b.isFeatured() ? 0 : 1)
                  .thenComparing(BookItem::getPopularity, Comparator.reverseOrder())
                  .thenComparing(BookItem::getTitle);
        };

    matched.sort(comparator);

    if (page <= 0 || pageSize <= 0) {
      return matched;
    }

    int fromIndex = (page - 1) * pageSize;
    if (fromIndex >= matched.size()) {
      return Collections.emptyList();
    }
    int toIndex = Math.min(fromIndex + pageSize, matched.size());
    return matched.subList(fromIndex, toIndex);
  }

  /** Returns count of matching books for search query and genre. */
  public static int countMatches(String query, String genre) {
    String q = (query == null) ? "" : query.trim().toLowerCase();
    String g = (genre == null) ? "" : genre.trim();

    return (int)
        ALL_BOOKS.stream()
            .filter(
                b -> {
                  if (!g.isEmpty() && !b.getGenre().equalsIgnoreCase(g)) return false;
                  if (q.isEmpty()) return true;
                  if (b.getTitle().toLowerCase().contains(q)) return true;
                  if (b.getAuthor().toLowerCase().contains(q)) return true;
                  if (b.getGenre().toLowerCase().contains(q)) return true;
                  for (String tag : b.getTags()) {
                    if (tag.toLowerCase().contains(q)) return true;
                  }
                  return b.getDescription().toLowerCase().contains(q);
                })
            .count();
  }

  /** Returns all books in a specific genre. */
  public static List<BookItem> getByGenre(String genre) {
    if (genre == null || genre.isBlank()) return Collections.emptyList();
    return ALL_BOOKS.stream()
        .filter(b -> b.getGenre().equalsIgnoreCase(genre.trim()))
        .collect(Collectors.toList());
  }

  /** Returns all featured books in the catalogue. */
  public static List<BookItem> getFeatured() {
    return ALL_BOOKS.stream()
        .filter(BookItem::isFeatured)
        .collect(Collectors.toList());
  }

  /**
   * Safely synchronizes all 108 books and 12 genres into the MySQL database tables
   * 'genres' and 'books'. Uses ON DUPLICATE KEY UPDATE so user collections,
   * reading progress, notes, and ratings are completely preserved.
   */
  public static void syncToDatabase(Connection c) throws SQLException {
    // 1. Ensure all 12 genres exist
    try (PreparedStatement psGenre =
        c.prepareStatement("INSERT IGNORE INTO genres(name) VALUES(?)")) {
      for (String g : GENRES) {
        psGenre.setString(1, g);
        psGenre.addBatch();
      }
      psGenre.executeBatch();
    }

    // 2. Fetch genre map (name -> id)
    Map<String, Integer> genreIds = new HashMap<>();
    try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM genres");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        genreIds.put(rs.getString("name"), rs.getInt("id"));
      }
    }

    // 3. Upsert all 108 books using stable IDs
    String upsertSql =
        "INSERT INTO books(id, title, author, genre_id, description, isbn, cover_image_url,"
            + " publication_year, total_pages, is_featured, is_demo) VALUES (?, ?, ?, ?, ?, ?, ?,"
            + " ?, ?, ?, 1) ON DUPLICATE KEY UPDATE title=VALUES(title), author=VALUES(author),"
            + " genre_id=VALUES(genre_id), description=VALUES(description), isbn=VALUES(isbn),"
            + " cover_image_url=VALUES(cover_image_url),"
            + " publication_year=VALUES(publication_year), total_pages=VALUES(total_pages),"
            + " is_featured=VALUES(is_featured)";

    try (PreparedStatement psBook = c.prepareStatement(upsertSql)) {
      for (BookItem b : ALL_BOOKS) {
        Integer gId = genreIds.get(b.getGenre());
        if (gId == null) gId = 1;

        psBook.setLong(1, b.getId());
        psBook.setString(2, b.getTitle());
        psBook.setString(3, b.getAuthor());
        psBook.setInt(4, gId);
        psBook.setString(5, b.getDescription());
        psBook.setString(6, b.getIsbn());
        psBook.setString(7, b.getCoverImage());
        psBook.setInt(8, b.getPublicationYear());
        psBook.setInt(9, b.getPageCount());
        psBook.setBoolean(10, b.isFeatured());
        psBook.addBatch();
      }
      psBook.executeBatch();
    }
  }
}
