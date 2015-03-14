%% ============================================================
\NeedsTeXFormat{LaTeX2e}[2005/12/01]
\ProvidesClass{assignment}[2011/09/01 v0.1 DIT.UoA Assignment LaTeX
  document class]

\LoadClass[a4paper,titlepage,twoside,12pt,leqno]{article}[2005/09/16]

%----------------------------------------------------------------------------------------
%	GENERAL PACKAGES
%----------------------------------------------------------------------------------------


% Τα πακέτα του xelatex που ορίζουν την ελληνική γλώσσα
\RequirePackage[]{fontspec}
\RequirePackage{xltxtra}
\RequirePackage[monogreek]{xgreek}

% Για υφενώσεις στα αγγλικά
\newcommand{\en}[1]{\setlanguage{american}#1\setlanguage{monogreek}} 

\setmainfont[]{DejaVu Sans} % Για τις γραμματοσειρές 
%\setsansfont[]{Ubuntu} 
\setmonofont[]{DejaVu Sans Mono} 


% Για τα μαθηματικά μέσω στο κείμενο (π.χ. \eqref)
\RequirePackage{amsmath}


%Να δω για να αλλάξω διάφορα
%http://www.mechpedia.gr/wiki/Hyphenation_-_%CE%A5%CF%86%CE%B5%CE%BD%CF%8E%CF%83%CE%B5%CE%B9%CF%82
%http://www.mechpedia.gr/wiki/%CE%95%CE%BB%CE%BB%CE%B7%CE%BD%CE%B9%CE%BA%CE%AC


\RequirePackage{hyperref} % In-text references
% \url command in bibliography

% Για να μην φαίνονται γραμμές γύρω από τις υπερσυνδέσεις μέσα στο κείμενο
\hypersetup{%
pdfborder = {0 0 0},
}


%----------------------------------------------------------------------------------------
%	END GENERAL PACKAGES
%----------------------------------------------------------------------------------------

%----------------------------------------------------------------------------------------
%	MARGIN OF TEXT PAGES%----------------------------------------------------------------------------------------

\RequirePackage[%
  a4paper,
  top=2cm,   bottom=2cm,     left=2cm,    right=2cm, bindingoffset=0.5cm, 
  head=14pt, headsep=0.50cm, foot=0.75cm, footskip=0.9cm,
  headsep=0.7cm ]{geometry}

%----------------------------------------------------------------------------------------
%	END MARGIN OF TEXT PAGE
%----------------------------------------------------------------------------------------

%----------------------------------------------------------------------------------------
%	FIGURES, SUBFIGURES AND CAPTIONS%----------------------------------------------------------------------------------------

\RequirePackage{tabularx} % Special tables. Επεκτείνει το περιβάλλον tabular
\RequirePackage{multirow} % Για την συγχώνευση πολλαπλών γραμμών στους πίνακες


% Για την αρίθμηση των πινάκων και των σχημάτων ανάλογα με τις ενότητες
\RequirePackage{chngcntr}
\counterwithin{figure}{section}
\counterwithin{table}{section}

\RequirePackage{subcaption}          % Subfigures
% Να κοιτάξω να το φτιάξω σωστά

% Captions
\RequirePackage[%
  justification=centering,
  labelsep=colon,
  figureposition=bottom,
  tableposition=top]{caption}          
% caption fonts should be boldface 10pt.
\renewcommand{\captionfont}{\fontsize{10pt}{12pt}\selectfont\bfseries}

%----------------------------------------------------------------------------------------
%	END FIGURES, SUBFIGURES AND CAPTIONS%----------------------------------------------------------------------------------------




%----------------------------------------------------------------------------------------
%	BIBLIOGRAPHY PACKAGES
%----------------------------------------------------------------------------------------

\RequirePackage[numbers]{natbib}


%Να το αλλάξω κάποια στιγμή σε biblatex
%http://tex.stackexchange.com/questions/25701/bibtex-vs-biber-and-biblatex-vs-natbib
%http://tex.stackexchange.com/questions/5091/what-to-do-to-switch-to-biblatex
%http://www.mechpedia.gr/wiki/Biblatex

%----------------------------------------------------------------------------------------
%	END BIBLIOGRAPHY PACKAGES
%----------------------------------------------------------------------------------------

%----------------------------------------------------------------------------------------
%	CODE INCLUSION CONFIGURATION
%----------------------------------------------------------------------------------------

\RequirePackage[outputdir=bin]{minted}
\usemintedstyle{borland}
%Να προσπαθήσω να ορίσω ως προεπιλογή το breaklines=true


%----------------------------------------------------------------------------------------
%	END CODE INCLUSION CONFIGURATION
%----------------------------------------------------------------------------------------
