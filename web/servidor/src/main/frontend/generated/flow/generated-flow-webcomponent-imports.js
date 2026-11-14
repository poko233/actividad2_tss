import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/combo-box/src/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/dialog/src/vaadin-dialog.js';
import '@vaadin/accordion/src/vaadin-accordion.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/tabs/src/vaadin-tab.js';
import '@vaadin/upload/src/vaadin-upload.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js';
import '@vaadin/tabs/src/vaadin-tabs.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-sorter.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/details/src/vaadin-details.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/grid/src/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/accordion/src/vaadin-accordion-panel.js';
import '@vaadin/notification/src/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '0d50b5486aac0b0b6bed92b5f62538187878454f282542eb12010ca1ce7d7bf2') {
    pending.push(import('./chunks/chunk-c8e53dcbbdeabf51e43fd06239c3d3eb0cb6dd240739a94b5fd57d970874708f.js'));
  }
  if (key === '6b532e03523a6611cf2a8400261c0afcf7def2202767e55abeb8ec5a7c768fdf') {
    pending.push(import('./chunks/chunk-a88188bd676a19481fd697f7780ff59f496e860d444cfeabef1d97587fda76f9.js'));
  }
  if (key === '454eb3d9b9b444a80b8a20f6a55eeeb6a3ab4f74d1abd7694557ff1af82e8c82') {
    pending.push(import('./chunks/chunk-66398f30fad3db65be5e5a81719407b3f27f13173ad21983c7bd291e90da5def.js'));
  }
  if (key === 'ef9056735503ef15836063d37be8bed816d9ec2f39d4931eab4db018a73b89a8') {
    pending.push(import('./chunks/chunk-c8e53dcbbdeabf51e43fd06239c3d3eb0cb6dd240739a94b5fd57d970874708f.js'));
  }
  if (key === '8811d7c2ab5d7903ac0d721ddd02c79f605e2b1b7b55e1bb14bede31fc6cac06') {
    pending.push(import('./chunks/chunk-66398f30fad3db65be5e5a81719407b3f27f13173ad21983c7bd291e90da5def.js'));
  }
  if (key === 'eba7582de0afb43b12dbd8faf11aa73bbba79eaf40e7734e34337f6e4884b95c') {
    pending.push(import('./chunks/chunk-c05e7a2e783c406737c663a66ce6631e0e17f37926a4db2af4e01e581d808424.js'));
  }
  if (key === 'e49da1e99eb4d858b6e7b4424f9bebec3225f732d35b46805e5209685946f182') {
    pending.push(import('./chunks/chunk-66398f30fad3db65be5e5a81719407b3f27f13173ad21983c7bd291e90da5def.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}