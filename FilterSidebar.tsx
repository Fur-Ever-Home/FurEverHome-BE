import React from 'react';
import { createPortal } from 'react-dom';
import { useLanguage } from '../useLanguage';
import { translations } from '../translations';
import './FilterSidebar.css';

export interface FilterState {
  adoptionType: string;
  species: string;
  gender: string;
  size: string;
  ageGroup: string;
  temperament: string;
  healthState: string;
  color: string;
  county: string;
  shelter: string;
  locationType: string;
  petFriendly: boolean;
  childFriendly: boolean;
  noAllergies: boolean;
  microchipped: boolean;
  vaccinated: boolean;
  sterilized: boolean;
  dewormed: boolean;
}

interface FilterSidebarProps {
  filters: FilterState;
  onFilterChange: (newFilters: FilterState) => void;
  onApply: () => void;
  onReset: () => void;
  totalResults?: number;
  isOpenMobile?: boolean;
  onCloseMobile?: () => void;
}

export const FilterSidebar: React.FC<FilterSidebarProps> = ({
  filters,
  onFilterChange,
  onApply,
  onReset,
  totalResults = 0,
  isOpenMobile = false,
  onCloseMobile,
}) => {
  const { lang } = useLanguage();
  const t = translations[lang as keyof typeof translations] || translations.ro;

  const handleToggle = (field: keyof FilterState, value: any) => {
    onFilterChange({
      ...filters,
      [field]: filters[field] === value ? '' : value,
    });
  };

  const handleCheckboxChange = (field: keyof FilterState) => {
    onFilterChange({
      ...filters,
      [field]: !filters[field],
    });
  };

  const handleSelectChange = (field: keyof FilterState, value: string) => {
    onFilterChange({
      ...filters,
      [field]: value,
    });
  };

  const renderFilterContent = () => (
    <div className="filter-body">
      {/* TIP ADOPȚIE */}
      <div className="filter-group">
        <label className="filter-group-title">{t.adoptionType || 'TIP ADOPȚIE'}</label>
        <div className="pill-group">
          <button
            type="button"
            className={`filter-pill ${filters.adoptionType === 'fizica' ? 'active' : ''}`}
            onClick={() => handleToggle('adoptionType', 'fizica')}
          >
            {t.physical || 'Fizică'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.adoptionType === 'la_distanta' ? 'active' : ''}`}
            onClick={() => handleToggle('adoptionType', 'la_distanta')}
          >
            {t.distance || 'La distanță'}
          </button>
        </div>

        {filters.adoptionType === 'la_distanta' && (
          <div className="distance-notice-box">
            <strong>{t.distanceNoticeTitle || 'Doar adopție la distanță'}</strong>
            <p>{t.distanceNoticeDesc || 'Se afișează doar seniorii și animalele care au nevoie de tratament sau cazare permanentă.'}</p>
          </div>
        )}
      </div>

      {/* INFO DE BAZĂ */}
      <div className="filter-group">
        <label className="filter-group-title">{t.basicInfo || 'INFO DE BAZĂ'}</label>

        <span className="sub-label">{t.type || 'Tip'}</span>
        <div className="pill-group">
          <button
            type="button"
            className={`filter-pill ${filters.species === 'Caine' ? 'active' : ''}`}
            onClick={() => handleToggle('species', 'Caine')}
          >
            {t.speciesDog || 'Câine'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.species === 'Pisica' ? 'active' : ''}`}
            onClick={() => handleToggle('species', 'Pisica')}
          >
            {t.speciesCat || 'Pisică'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.species === 'Altele' ? 'active' : ''}`}
            onClick={() => handleToggle('species', 'Altele')}
          >
            {t.speciesOther || 'Altele'}
          </button>
        </div>

        <span className="sub-label">{t.sex || 'Sex'}</span>
        <div className="pill-group">
          <button
            type="button"
            className={`filter-pill ${filters.gender === 'Mascul' ? 'active' : ''}`}
            onClick={() => handleToggle('gender', 'Mascul')}
          >
            {t.male || 'Mascul'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.gender === 'Femela' ? 'active' : ''}`}
            onClick={() => handleToggle('gender', 'Femela')}
          >
            {t.female || 'Femelă'}
          </button>
        </div>

        <span className="sub-label">{t.size || 'Talie'}</span>
        <div className="pill-group">
          <button
            type="button"
            className={`filter-pill ${filters.size === 'Mica' ? 'active' : ''}`}
            onClick={() => handleToggle('size', 'Mica')}
          >
            {t.small || 'Mică'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.size === 'Medie' ? 'active' : ''}`}
            onClick={() => handleToggle('size', 'Medie')}
          >
            {t.medium || 'Medie'}
          </button>
          <button
            type="button"
            className={`filter-pill ${filters.size === 'Mare' ? 'active' : ''}`}
            onClick={() => handleToggle('size', 'Mare')}
          >
            {t.large || 'Mare'}
          </button>
        </div>
      </div>

      {/* DETALII ANIMAL */}
      <div className="filter-group">
        <label className="filter-group-title">{t.animalDetails || 'DETALII ANIMAL'}</label>
        
        <select 
          className="filter-select"
          value={filters.ageGroup}
          onChange={(e) => handleSelectChange('ageGroup', e.target.value)}
        >
          <option value="">{t.age || 'Vârstă'}</option>
          <option value="pui">{t.agePuppy || 'Pui (< 1 an)'}</option>
          <option value="tanar">{t.ageYoung || 'Tânăr (1 - 3 ani)'}</option>
          <option value="adult">{t.ageAdult || 'Adult (3 - 7 ani)'}</option>
          <option value="senior">{t.ageSenior || 'Senior (> 7 ani)'}</option>
        </select>

        <select 
          className="filter-select"
          value={filters.temperament}
          onChange={(e) => handleSelectChange('temperament', e.target.value)}
        >
          <option value="">{t.temperament || 'Temperament'}</option>
          <option value="jucaus">{t.playful || 'Jucăuș'}</option>
          <option value="calm">{t.calm || 'Calm'}</option>
          <option value="energic">{t.energetic || 'Energic'}</option>
          <option value="timid">{t.shy || 'Timid'}</option>
        </select>

        <select 
          className="filter-select"
          value={filters.healthState}
          onChange={(e) => handleSelectChange('healthState', e.target.value)}
        >
          <option value="">{t.healthState || 'Stare de sănătate'}</option>
          <option value="sanatos">{t.healthy || 'Sănătos'}</option>
          <option value="necesita_ingrijiri">{t.needsCare || 'Necesită îngrijiri'}</option>
        </select>

        <select 
          className="filter-select"
          value={filters.color}
          onChange={(e) => handleSelectChange('color', e.target.value)}
        >
          <option value="">{t.color || 'Culoare'}</option>
          <option value="negru">{t.colorBlack || 'Negru'}</option>
          <option value="alb">{t.colorWhite || 'Alb'}</option>
          <option value="maro">{t.colorBrown || 'Maro'}</option>
          <option value="portocaliu">{t.colorOrange || 'Portocaliu / Roșcat'}</option>
          <option value="bicolor">{t.colorBicolor || 'Bicolor / Tricolor'}</option>
          {/*daria*/}
          <option value="gri">{lang === 'en' ? 'Grey' : 'Gri'}</option>
          <option value="crem">{lang === 'en' ? 'Cream' : 'Crem'}</option>
          <option value="bej">{lang === 'en' ? 'Beige' : 'Bej'}</option>
          <option value="auriu">{lang === 'en' ? 'Golden' : 'Auriu'}</option>
          <option value="tabby">Tabby</option>
          <option value="tricolor">Tricolor</option>
          <option value="sable">Sable</option>
          <option value="verde">{lang === 'en' ? 'Green' : 'Verde'}</option>
          <option value="roz">{lang === 'en' ? 'Pink' : 'Roz'}</option>
        </select>
      </div>

      {/* LOCAȚIE */}
      <div className="filter-group">
        <label className="filter-group-title">{t.locationLabel || 'LOCAȚIE'}</label>

        <select 
          className="filter-select"
          value={filters.county}
          onChange={(e) => handleSelectChange('county', e.target.value)}
        >
          <option value="">{t.county || 'Județ'}</option>
          <option value="iasi">Iași</option>
          <option value="bucuresti">București</option>
          <option value="cluj">Cluj</option>
          <option value="timis">Timiș</option>
        </select>

        <select 
          className="filter-select"
          value={filters.shelter}
          onChange={(e) => handleSelectChange('shelter', e.target.value)}
        >
          <option value="">{t.shelter || 'Centru / Adăpost'}</option>
          <option value="casa_lui_azor">Casa lui Azor</option>
          <option value="labute_copou">Adăpostul Lăbuțe din Copou</option>
          <option value="patrupezi_iasi">Asociația Patrupezi Iași</option>
        </select>

        <select 
          className="filter-select"
          value={filters.locationType}
          onChange={(e) => handleSelectChange('locationType', e.target.value)}
        >
          <option value="">{t.locationTypeLabel || 'Locația animalului'}</option>
          <option value="centru">{t.center || 'Centru / Adăpost'}</option>
          <option value="foster">{t.foster || 'În Foster'}</option>
        </select>
      </div>

      {/* COMPATIBILITATE & SĂNĂTATE */}
      <div className="filter-group">
        <label className="filter-group-title">{t.compatibilityHealth || 'COMPATIBILITATE & SĂNĂTATE'}</label>

        <div className="checkbox-grid">
          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.petFriendly} 
              onChange={() => handleCheckboxChange('petFriendly')} 
            />
            <span>{t.petFriendly || 'Pet friendly'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.microchipped} 
              onChange={() => handleCheckboxChange('microchipped')} 
            />
            <span>{t.microchipped || 'Microcipat'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.childFriendly} 
              onChange={() => handleCheckboxChange('childFriendly')} 
            />
            <span>{t.childFriendly || 'Child friendly'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.vaccinated} 
              onChange={() => handleCheckboxChange('vaccinated')} 
            />
            <span>{t.vaccinated || 'Vaccinat'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.noAllergies} 
              onChange={() => handleCheckboxChange('noAllergies')} 
            />
            <span>{t.noAllergies || 'Fără alergii'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.sterilized} 
              onChange={() => handleCheckboxChange('sterilized')} 
            />
            <span>{t.sterilized || 'Sterilizat'}</span>
          </label>

          <label className="checkbox-label">
            <input 
              type="checkbox" 
              checked={filters.dewormed} 
              onChange={() => handleCheckboxChange('dewormed')} 
            />
            <span>{t.dewormed || 'Deparazitat'}</span>
          </label>
        </div>
      </div>
    </div>
  );

  return (
    <>
      <aside className="filter-sidebar-desktop">
        <h3 className="filter-sidebar-title">{t.filterBy || 'Filtrează după'}</h3>
        {renderFilterContent()}
        <div className="filter-sidebar-actions">
          <button type="button" className="btn-apply-filters" onClick={onApply}>
            {t.applyFilters || 'Aplică filtre'}
          </button>
          <button type="button" className="btn-reset-filters" onClick={onReset}>
            {t.resetFilters || 'Resetează filtrele'}
          </button>
        </div>
      </aside>

      {isOpenMobile && createPortal(
        <div className="filter-mobile-overlay" onClick={onCloseMobile}>
          <div className="filter-mobile-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="filter-mobile-header">
              <button type="button" className="filter-mobile-close" onClick={onCloseMobile}>
                ✕
              </button>
              <h3 className="filter-mobile-title">{t.filterBy || 'Filtrează după'}</h3>
              <button type="button" className="filter-mobile-clear" onClick={onReset}>
                {t.clearAll || 'Șterge tot'}
              </button>
            </div>

            <div className="filter-mobile-scroll-body">
              {renderFilterContent()}
            </div>

            <div className="filter-mobile-footer">
              <button 
                type="button" 
                className="btn-mobile-apply" 
                onClick={() => {
                  onApply();
                  if (onCloseMobile) onCloseMobile();
                }}
              >
                {t.applyFilters || 'Aplică filtre'} ({totalResults})
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </>
  );
};

export default FilterSidebar;