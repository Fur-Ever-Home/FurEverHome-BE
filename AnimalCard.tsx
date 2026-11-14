import React from 'react';
import FavoriteBtn from './FavoriteBtn';
import { useLanguage } from '../useLanguage';
import { translations } from '../translations';
import vaccineIcon from '../assets/vaccinat.png';
import dewormIcon from '../assets/deparazitat.png';
import sterileIcon from '../assets/sterlizat.png';
import dogIcon from '../assets/dog-icon.svg';
import './AnimalCard.css';

export interface AnimalCardProps {
  animal: {
    id: number | string;
    name: string;
    species: string;
    gender?: string;
    sex?: string;
    age: string | number;
    size: string;
    shelter?: string;
    location?: string;
    status?: string;
    vaccinated?: boolean;
    dewormed?: boolean;
    sterilized?: boolean;
    imageUrl?: string;
    image?: string;
    fundingPercent?: number;
    needType?: string;
  };
  isDistanceMode?: boolean;
}

export const AnimalCard: React.FC<AnimalCardProps> = ({ animal, isDistanceMode = false }) => {
  const { lang } = useLanguage();
  const t = translations[lang as keyof typeof translations] || translations.ro;
  const isLoggedIn = !!localStorage.getItem('token');

  const imageSrc = animal.imageUrl || animal.image || dogIcon;
  const genderValue = animal.gender || animal.sex || '';

  const translateGender = (g: string) => {
    if (!g) return '';
    const lower = g.toLowerCase().trim();
    if (lower.includes('masc') || lower === 'male') return t.male || 'Mascul';
    if (lower.includes('fem') || lower === 'female') return t.female || 'Femelă';
    return g;
  };

  const translateSize = (s: string) => {
    if (!s) return '';
    const lower = s.toLowerCase().trim();
    if (lower.includes('medie') || lower.includes('medium')) return lang === 'ro' ? 'talie medie' : 'medium size';
    if (lower.includes('mare') || lower.includes('large')) return lang === 'ro' ? 'talie mare' : 'large size';
    if (lower.includes('mic') || lower.includes('small')) return lang === 'ro' ? 'talie mică' : 'small size';
    return s;
  };

  const translateStatus = (st?: string) => {
    if (!st) return t.statusAvailable || 'Disponibil';
    const lower = st.toLowerCase().trim();
    if (lower.includes('disponibil') || lower.includes('available')) return t.statusAvailable || 'Disponibil';
    if (lower.includes('rezervat') || lower.includes('reserved')) return t.statusReserved || 'Rezervat';
    if (lower.includes('adoptat') || lower.includes('adopted')) return t.statusAdopted || 'Adoptat';
    return st;
  };

  const translateAge = (age: string | number) => {
    if (!age) return '';
    const ageStr = String(age).toLowerCase().trim();
    if (lang === 'ro') return ageStr;
    return ageStr
      .replace(/luni/g, t.months || 'months')
      .replace(/ani/g, t.yearsOld || 'years old')
      .replace(/an\b/g, t.yearOld || 'year old');
  };

  const translateNeedType = (need?: string) => {
    if (!need) return lang === 'en' ? 'Permanent care' : 'Cazare permanentă';
    if (lang === 'ro') return need;
    const lower = need.toLowerCase();
    if (lower.includes('cazare')) return 'Permanent care';
    if (lower.includes('tratament continuu')) return 'Continuous treatment';
    if (lower.includes('tratament')) return 'Treatment & care';
    return need;
  };

  return (
    <div className={`animal-card ${isDistanceMode ? 'distance-card-style' : ''}`}>
      <div className="card-image-wrapper">
        {!isDistanceMode && (
  <span 
    className="status-badge-card"
    style={isDistanceMode ? { backgroundColor: '#A66E38', color: '#FFFDF8' } : undefined}
  >
    {translateStatus(animal.status)}
  </span>
)}
        
        <img src={imageSrc} alt={animal.name} className="animal-card-img" />

        <div className="card-fav-btn">
          <FavoriteBtn animal={animal} isLoggedIn={isLoggedIn} />
        </div>
      </div>

      <div className="card-content">
        {/* Iconițele de sănătate poziționate sub imagine, ca în design */}
        {!isDistanceMode && (
          <div className="card-health-badges">
            <span className={`health-badge-circle ${animal.vaccinated !== false ? 'active' : 'inactive'}`} title={t.vaccinated || 'Vaccinat'}>
              <img src={vaccineIcon} alt={t.vaccinated || 'Vaccinat'} />
            </span>
            <span className={`health-badge-circle ${animal.dewormed !== false ? 'active' : 'inactive'}`} title={t.dewormed || 'Deparazitat'}>
              <img src={dewormIcon} alt={t.dewormed || 'Deparazitat'} />
            </span>
            <span className={`health-badge-circle ${animal.sterilized !== false ? 'active' : 'inactive'}`} title={t.sterilized || 'Sterilizat'}>
              <img src={sterileIcon} alt={t.sterilized || 'Sterilizat'} />
            </span>
          </div>
        )}

        <h3 
  className="card-title" 
  style={isDistanceMode ? { marginTop: '16px' } : undefined}
>
  {animal.name}
</h3>
        <p className="card-subtitle">
          {translateGender(genderValue)} · {translateAge(animal.age)} · {translateSize(animal.size)}
        </p>
        <p className="card-location">{animal.shelter || animal.location || 'Casa lui Azor'}</p>

        {isDistanceMode && (
          <>
            <div style={{ marginTop: '6px', marginBottom: '8px' }}>
              <span style={{
                display: 'inline-block',
                backgroundColor: '#EFE6D8',
                color: '#5C4E43',
                fontSize: '11px',
                fontWeight: 600,
                padding: '4px 10px',
                borderRadius: '12px'
              }}>
                {translateNeedType(animal.needType)}
              </span>
            </div>

           {/*<div className="distance-progress-container">
              <div className="progress-bar-bg">
                <div 
                  className="progress-bar-fill" 
                  style={{ width: `${animal.fundingPercent || 45}%` }}
                />
              </div>
              <span className="progress-text">
                {animal.fundingPercent || 45}% {t.fundingCovered || 'din necesarul lunar acoperit'}
              </span>
            </div>*/}
          
             <div className="distance-progress-container">
              <div className="progress-bar-bg">
                <div 
                  className="progress-bar-fill" 
                  style={{ width: `${animal.fundingPercent ?? 0}%` }}
                />
              </div>
              <span className="progress-text">
                {animal.fundingPercent ?? 0}% {t.fundingCovered || 'din necesarul lunar acoperit'}
              </span>
            </div>

          </>
        )}

        <button 
          type="button" 
          className="btn-card-details"
          style={isDistanceMode ? { 
            backgroundColor: '#A66E38', 
            color: '#FFFDF8',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px'
          } : undefined}
        >
          {isDistanceMode ? (
            <>
              {lang === 'en' ? 'View details' : 'Vezi detalii'}
            </>
          ) : (
            t.btnViewDetails || 'Vezi detalii'
          )}
        </button>
      </div>
    </div>
  );
};

export default AnimalCard;