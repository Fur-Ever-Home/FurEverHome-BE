import { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import FloatingBtn from '../components/floating-btn';
import { AnimalCard } from '../components/AnimalCard';
import { AnimalModal } from '../components/AnimalModal';
import { useLanguage } from '../useLanguage';
import { translations } from '../translations';
import { API_BASE_URL } from '../config';
import dogIcon from '../assets/dog-icon.svg';
import FilterSidebar, { FilterState } from '../components/FilterSidebar';
import './Catalog.css';

interface Animal {
  id: number;
  name: string;
  species: string;
  status: string;
  gender: string;
  age: string;
  size: string;
  shelter: string;
  vaccinated: boolean;
  dewormed: boolean;
  sterilized: boolean;
  imageUrl: string;
  breed?: string;
  sex?: string;
  temperament?: string;
  color?: string;
  location?: string;
  locationType?: string;
  images?: string[];
  microchipped?: boolean;
  petFriendly?: boolean;
  childFriendly?: boolean;
  noAllergies?: boolean;
  date?: string;
  createdAt?: string;
  isDistanceAdoption?: boolean;
  needType?: string;
  fundingPercent?: number;
}

const initialFilterState: FilterState = {
  adoptionType: '',
  species: '',
  gender: '',
  size: '',
  ageGroup: '',
  temperament: '',
  healthState: '',
  color: '',
  county: '',
  shelter: '',
  locationType: '',
  petFriendly: false,
  childFriendly: false,
  noAllergies: false,
  microchipped: false,
  vaccinated: false,
  sterilized: false,
  dewormed: false,
};

export default function Catalog() {
  const [searchParams] = useSearchParams();
  const categorieParam = searchParams.get('categorie');
  const tipParam = searchParams.get('tip');
  const navigate = useNavigate();

  const { lang } = useLanguage();
  const t = translations[lang as keyof typeof translations] || translations.ro;

  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedAnimal, setSelectedAnimal] = useState<any | null>(null);
  const [sortOrder, setSortOrder] = useState('noi');

  const [filters, setFilters] = useState<FilterState>({
    ...initialFilterState,
    adoptionType: tipParam === 'la_distanta' ? 'la_distanta' : '',
  });
  const [isMobileFilterOpen, setIsMobileFilterOpen] = useState(false);

  const mobileTrackRef = useRef<HTMLDivElement>(null);

  {/*const fetchAnimals = () => {
    setLoading(true);

    const queryParams = new URLSearchParams();
    if (filters.adoptionType) queryParams.append('adoptionType', filters.adoptionType);
    if (filters.species) queryParams.append('species', filters.species);
    if (filters.gender) queryParams.append('gender', filters.gender);
    if (filters.size) queryParams.append('size', filters.size);
    if (filters.shelter) queryParams.append('shelter', filters.shelter);
    if (filters.vaccinated) queryParams.append('vaccinated', 'true');
    if (filters.sterilized) queryParams.append('sterilized', 'true');
    if (filters.dewormed) queryParams.append('dewormed', 'true');

    const queryString = queryParams.toString();
    const url = `${API_BASE_URL}/api/animals${queryString ? `?${queryString}` : ''}`;

    fetch(url)
      .then((response) => response.json())
      .then((data) => {
        setAnimals(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Eroare la preluarea animalelor:', error);
        setLoading(false);
      });
  };*/}
  const fetchAnimals = () => {
    setLoading(true);

    const queryParams = new URLSearchParams();
    if (filters.adoptionType) queryParams.append('adoptionType', filters.adoptionType);
    if (filters.species) queryParams.append('species', filters.species);
    if (filters.gender) queryParams.append('gender', filters.gender);
    if (filters.size) queryParams.append('size', filters.size);
    if (filters.ageGroup) queryParams.append('ageGroup', filters.ageGroup);
    if (filters.temperament) queryParams.append('temperament', filters.temperament);
    if (filters.color) queryParams.append('color', filters.color);
    if (filters.shelter) queryParams.append('shelter', filters.shelter);
    if (filters.locationType) queryParams.append('locationType', filters.locationType);
    if (filters.petFriendly) queryParams.append('petFriendly', 'true');
    if (filters.childFriendly) queryParams.append('childFriendly', 'true');
    if (filters.vaccinated) queryParams.append('vaccinated', 'true');
    if (filters.sterilized) queryParams.append('sterilized', 'true');
    if (filters.dewormed) queryParams.append('dewormed', 'true');

    const queryString = queryParams.toString();
    const url = `${API_BASE_URL}/api/animals${queryString ? `?${queryString}` : ''}`;

    fetch(url)
      .then((response) => response.json())
      .then((data) => {
        setAnimals(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Eroare la preluarea animalelor:', error);
        setLoading(false);
      });
  };

  useEffect(() => {
    if (categorieParam) {
      setFilters((prev) => ({ ...prev, species: categorieParam }));
    }
  }, [categorieParam]);

  {/*useEffect(() => {
    fetchAnimals();
  }, [categorieParam, filters.adoptionType]);*/}
  useEffect(() => {
    fetchAnimals();
  }, [categorieParam, filters]);

  {/*const handleResetFilters = () => {
    setFilters(initialFilterState);
    fetchAnimals();
  };*/

  const handleResetFilters = () => {
    setFilters(initialFilterState);
    setLoading(true);
    fetch(`${API_BASE_URL}/api/animals`)
      .then((response) => response.json())
      .then((data) => {
        setAnimals(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Eroare la resetarea animalelor:', error);
        setLoading(false);
      });
  }; 

  const animaleSortate = [...animals].sort((a, b) => {
    const dateA = new Date(a.date || a.createdAt || Date.now()).getTime();
    const dateB = new Date(b.date || b.createdAt || Date.now()).getTime();
    if (sortOrder === 'noi') {
      return dateB - dateA;
    }
    return dateA - dateB;
  });

  const handleMobileScroll = (direction: 'left' | 'right') => {
    if (mobileTrackRef.current) {
      const scrollAmount = 296;
      mobileTrackRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  const handleCardClick = (animalId: number, animalDataForModal: any) => {
    if (window.innerWidth <= 768) {
      // ADAUGĂM state-ul care spune de unde venim
      navigate(`/animal/${animalId}${isDistanceMode ? '?tip=la_distanta' : ''}`, {
        state: { animal: animalDataForModal, isDistanceMode, from: 'catalog' }
      });
    } else {
      setSelectedAnimal(animalDataForModal);
    }
  };

  const isDistanceMode = filters.adoptionType === 'la_distanta';

  return (
    <div className="catalog-page">
      <style>{`
        .custom-mobile-header {
          display: none !important;
        }

        @media (max-width: 768px) {
          .custom-mobile-header {
            display: block !important;
          }
          .catalog-mobile-filter-bar,
          .catalog-header-row {
            display: none !important;
          }
          .mobile-arrow-btn {
            display: none !important;
          }
          .mobile-cards-track {
            display: flex !important;
            flex-direction: column !important;
            gap: 20px !important;
            overflow: visible !important;
          }
          .mobile-card-slide {
            width: 100% !important;
            max-width: 100% !important;
          }
        }

        .catalog-card-item > div, .mobile-card-slide > div {
          width: 100% !important;
          max-width: 100% !important;
          margin: 0 !important;
          box-sizing: border-box !important;
          overflow: hidden !important;
        }
        .catalog-card-item .animal-card-img, .mobile-card-slide .animal-card-img {
          width: 100% !important;
          height: 250px !important;
          max-width: none !important;
          object-fit: cover !important;
          object-position: center center !important;
          display: block !important;
          margin: 0 auto !important;
          padding: 0 !important;
          border-radius: 0 !important;
        }
        .catalog-card-item .card-health-overlay img, .mobile-card-slide .card-health-overlay img {
          width: 32px !important;
          height: 32px !important;
          max-width: 32px !important;
          max-height: 32px !important;
          object-fit: contain !important;
          border-radius: 50% !important;
          display: block !important;
        }

        .modal-info-scroll, .animal-modal-details {
          max-height: 380px !important;
          overflow-y: scroll !important;
          padding-right: 14px !important;
          scrollbar-width: thin !important;
          scrollbar-color: #C68642 #EFE6D8 !important;
        }
        .modal-info-scroll::-webkit-scrollbar, .animal-modal-details::-webkit-scrollbar {
          width: 8px !important;
          display: block !important;
        }
        .modal-info-scroll::-webkit-scrollbar-track, .animal-modal-details::-webkit-scrollbar-track {
          background: #EFE6D8 !important;
          border-radius: 8px !important;
        }
        .modal-info-scroll::-webkit-scrollbar-thumb, .animal-modal-details::-webkit-scrollbar-thumb {
          background-color: #C68642 !important;
          border-radius: 8px !important;
          border: 2px solid #EFE6D8 !important;
        }
        .modal-info-scroll::-webkit-scrollbar-thumb:hover, .animal-modal-details::-webkit-scrollbar-thumb:hover {
          background-color: #3F2C20 !important;
        }
      `}</style>

      <Header />
      
      <div className="catalog-layout-container">
        <FilterSidebar
          filters={filters}
          onFilterChange={setFilters}
          onApply={fetchAnimals}
          onReset={handleResetFilters}
          totalResults={animaleSortate.length}
          isOpenMobile={isMobileFilterOpen}
          onCloseMobile={() => setIsMobileFilterOpen(false)}
        />

        <main className="catalog-main-content">
          <div className="custom-mobile-header">
            <h1 style={{ fontSize: '26px', lineHeight: '1.15', color: '#3F2C20', margin: '0 0 4px 0', fontFamily: 'serif', fontWeight: 'bold' }}>
              {isDistanceMode 
                ? (lang === 'en' ? 'Support a friend' : 'Sprijină un prieten')
                : (lang === 'en' ? 'Find your future friend.' : 'Găsește-ți viitorul prieten.')}
            </h1>
            <h2 style={{ fontSize: '22px', lineHeight: '1.2', color: '#C68642', margin: '0 0 6px 0', fontFamily: 'serif', fontWeight: 'bold' }}>
              {isDistanceMode
                ? (lang === 'en' ? 'from a distance.' : 'de la distanță.')
                : (lang === 'en' ? 'Offer a warm home.' : 'Oferă-i un cămin cald.')}
            </h2>
            <p style={{ fontSize: '12px', color: '#5c4e43', margin: '0 0 14px 0' }}>
              {isDistanceMode
                ? (lang === 'en' ? 'Seniors and animals with special needs.' : 'Seniori și animale cu nevoi speciale.')
                : (lang === 'en' ? 'Every soul deserves a chance.' : 'Fiecare suflet merită o șansă.')}
            </p>

            <p style={{ fontSize: '11px', fontWeight: '700', color: '#6b5b4f', margin: '0 0 6px 0' }}>
              {lang === 'en' ? 'Adoption type' : 'Tip adopție'}
            </p>
            <div style={{ display: 'flex', gap: '8px', marginBottom: '14px' }}>
              <button 
                type="button"
                onClick={() => setFilters((prev) => ({ ...prev, adoptionType: '' }))}
                style={{ 
                  padding: '6px 16px', borderRadius: '20px', cursor: 'pointer', fontSize: '12px', fontWeight: '600',
                  border: !isDistanceMode ? '1px solid #3F2C20' : '1px solid #8c7b6c', 
                  backgroundColor: !isDistanceMode ? '#3F2C20' : 'transparent', 
                  color: !isDistanceMode ? '#FFFDF8' : '#3F2C20' 
                }}
              >
                {lang === 'en' ? 'Physical' : 'Fizică'}
              </button>
              <button 
                type="button"
                onClick={() => setFilters((prev) => ({ ...prev, adoptionType: 'la_distanta' }))}
                style={{ 
                  padding: '6px 16px', borderRadius: '20px', cursor: 'pointer', fontSize: '12px', fontWeight: '600',
                  border: isDistanceMode ? '1px solid #3F2C20' : '1px solid #8c7b6c', 
                  backgroundColor: isDistanceMode ? '#3F2C20' : 'transparent', 
                  color: isDistanceMode ? '#FFFDF8' : '#3F2C20' 
                }}
              >
                {lang === 'en' ? 'Remote' : 'La distanță'}
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
              <button 
                type="button"
                onClick={() => setIsMobileFilterOpen(true)}
                style={{ 
                  display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 16px', borderRadius: '20px', 
                  border: 'none', backgroundColor: '#3F2C20', color: '#FFFDF8', fontSize: '12px', fontWeight: '600', cursor: 'pointer' 
                }}
              >
                {lang === 'en' ? 'Filter' : 'Filtrează'} <span>⚙️</span>
              </button>

              <select 
                value={sortOrder} 
                onChange={(e) => setSortOrder(e.target.value)}
                style={{ 
                  padding: '7px 14px', borderRadius: '20px', border: '1px solid #DFCEB8', 
                  backgroundColor: '#FFFDF8', color: '#3F2C20', fontSize: '12px', fontWeight: '500', cursor: 'pointer'
                }}
              >
                <option value="noi">{lang === 'en' ? 'Sort' : 'Sortează'}</option>
                <option value="vechi">{lang === 'en' ? 'Oldest' : 'Cele mai vechi'}</option>
              </select>
            </div>

            <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px', marginBottom: '12px' }}>
              {[
                { val: 'caine', labelRo: 'Câini', labelEn: 'Dogs' },
                { val: 'pisica', labelRo: 'Pisici', labelEn: 'Cats' },
                { val: 'altele', labelRo: 'Alte animale', labelEn: 'Other animals' }
              ].map((cat) => {
                const isActive = filters.species.toLowerCase() === cat.val;
                return (
                  <button
                    key={cat.val}
                    type="button"
                    onClick={() => setFilters((prev) => ({ ...prev, species: isActive ? '' : cat.val }))}
                    style={{
                      padding: '5px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: '600', cursor: 'pointer', whiteSpace: 'nowrap',
                      border: isActive ? '1px solid #3F2C20' : '1px solid #8c7b6c',
                      backgroundColor: isActive ? '#3F2C20' : 'transparent',
                      color: isActive ? '#FFFDF8' : '#3F2C20'
                    }}
                  >
                    {lang === 'en' ? cat.labelEn : cat.labelRo}
                  </button>
                );
              })}
            </div>

            <p style={{ fontWeight: '700', color: '#3F2C20', fontSize: '14px', margin: '0 0 12px 0' }}>
              {isDistanceMode
                ? (lang === 'en' ? `${animaleSortate.length} friends waiting for a sponsor` : `${animaleSortate.length} prieteni așteaptă un sponsor`)
                : (lang === 'en' ? `${animaleSortate.length} friends are looking for a home` : `${animaleSortate.length} prieteni își caută o casă`)}
            </p>
          </div>

          <div className="catalog-mobile-filter-bar">
            <button 
              type="button" 
              className="btn-open-mobile-filters"
              onClick={() => setIsMobileFilterOpen(true)}
            >
              ⚙️ {lang === 'en' ? 'Filters' : 'Filtrează'} ({animaleSortate.length})
            </button>
          </div>

          <div className="catalog-header-row">
            <h1 className="catalog-title">
              {isDistanceMode
                ? (lang === 'en' ? `${animaleSortate.length} friends waiting for a sponsor` : `${animaleSortate.length} prieteni așteaptă un sponsor`)
                : (lang === 'en' ? `${animaleSortate.length} friends are looking for a home` : `${animaleSortate.length} prieteni își caută o casă`)}
            </h1>
            
            <div className="catalog-sort-wrapper">
              <label className="catalog-sort-label">
                {lang === 'en' ? 'Sort by:' : 'Sortează după:'}
              </label>
              <select 
                className="catalog-sort-select"
                value={sortOrder} 
                onChange={(e) => setSortOrder(e.target.value)}
              >
                <option value="noi">{lang === 'en' ? 'Newest' : 'Cele mai noi'}</option>
                <option value="vechi">{lang === 'en' ? 'Oldest' : 'Cele mai vechi'}</option>
              </select>
            </div>
          </div>

          {isDistanceMode && (
            <div className="distance-banner-card">
              <div className="distance-banner-icon">🤎</div>
              <div className="distance-banner-text">
                <h3>{t.distanceBannerTitle || 'Adopția la distanță'}</h3>
                <p>{t.distanceBannerDesc || 'Sprijină îngrijirea, tratamentul și cazarea unui animal care nu mai poate fi adoptat fizic.'}</p>
              </div>
            </div>
          )}
          
          {filters.species && !isDistanceMode && (
            <div className="catalog-filter-badge">
              {lang === 'en' ? 'Active filter: Showing category' : 'Filtru activ: Se afișează categoria'}{' '}
              <span className="catalog-filter-highlight">{filters.species}</span>.
            </div>
          )}

          <div className="catalog-cards-grid desktop-only-grid">
            {loading ? (
              <p className="catalog-message">{t.loading || 'Se încarcă animalele...'}</p>
            ) : animaleSortate.length === 0 ? (
              <p className="catalog-message">
                {lang === 'en' ? 'No animals found.' : 'Nu s-au găsit animale conform filtrelor selectate.'}
              </p>
            ) : (
              animaleSortate.map((animal) => {
                const animalDataForModal = {
                  ...animal,
                  breed: animal.breed || "Rasă comună",
                  sex: animal.gender || animal.sex || "Femelă",
                  age: animal.age || "1 an",
                  size: animal.size || "Medie",
                  temperament: animal.temperament || "Jucăuș",
                  color: animal.color || "Portocaliu",
                  location: animal.shelter || animal.location || "Centrul Casa lui Azor",
                  locationType: animal.locationType || "Centru",
                  status: animal.status || "Disponibil",
                  images: animal.images && animal.images.length > 0 
                    ? animal.images 
                    : [animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon],
                  microchipped: animal.microchipped ?? true,
                  petFriendly: animal.petFriendly ?? true,
                  childFriendly: animal.childFriendly ?? false,
                  noAllergies: animal.noAllergies ?? true,
                };

                return (
                  <div 
                    key={animal.id} 
                    onClick={() => handleCardClick(animal.id, animalDataForModal)} 
                    className="catalog-card-item"
                  >
                    <AnimalCard 
                      animal={{ 
                        ...animal, 
                        imageUrl: animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon,
                        image: animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon
                      } as any}
                      /*isDistanceMode={isDistanceMode}*/
                      isDistanceMode={isDistanceMode || (animal as any).adoptionType === 'distanta'}
                    />
                  </div>
                );
              })
            )}
          </div>

          <div className="mobile-carousel-wrapper">
            <button 
              type="button" 
              className="mobile-arrow-btn left" 
              onClick={() => handleMobileScroll('left')}
              aria-label="Anterior"
            >
              ‹
            </button>

            <div className="mobile-cards-track" ref={mobileTrackRef}>
              {animaleSortate.map((animal) => {
                const animalDataForModal = {
                  ...animal,
                  breed: animal.breed || "Rasă comună",
                  sex: animal.gender || animal.sex || "Femelă",
                  age: animal.age || "1 an",
                  size: animal.size || "Medie",
                  temperament: animal.temperament || "Jucăuș",
                  color: animal.color || "Portocaliu",
                  location: animal.shelter || animal.location || "Centrul Casa lui Azor",
                  locationType: animal.locationType || "Centru",
                  status: animal.status || "Disponibil",
                  images: animal.images && animal.images.length > 0 
                    ? animal.images 
                    : [animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon],
                  microchipped: animal.microchipped ?? true,
                  petFriendly: animal.petFriendly ?? true,
                  childFriendly: animal.childFriendly ?? false,
                  noAllergies: animal.noAllergies ?? true,
                };

                return (
                  <div 
                    key={animal.id} 
                    onClick={() => handleCardClick(animal.id, animalDataForModal)} 
                    className="mobile-card-slide"
                  >
                    <AnimalCard 
                      animal={{ 
                        ...animal, 
                        imageUrl: animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon,
                        image: animal.imageUrl && animal.imageUrl.trim() !== "" ? animal.imageUrl : dogIcon
                      } as any}
                      /*isDistanceMode={isDistanceMode}*/
                      isDistanceMode={isDistanceMode || (animal as any).adoptionType === 'distanta'} 
                    />
                  </div>
                );
              })}
            </div>

            <button 
              type="button" 
              className="mobile-arrow-btn right" 
              onClick={() => handleMobileScroll('right')}
              aria-label="Următor"
            >
              ›
            </button>
          </div>

        </main>
      </div>

      {selectedAnimal && (
        <AnimalModal 
          animal={selectedAnimal}
          onClose={() => setSelectedAnimal(null)} 
          isDistanceMode={isDistanceMode}
        />
      )}

      <Footer />
      <FloatingBtn />
    </div>
  );
} }