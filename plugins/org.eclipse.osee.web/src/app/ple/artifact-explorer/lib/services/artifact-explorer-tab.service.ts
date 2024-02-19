/*********************************************************************
 * Copyright (c) 2023 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Injectable, inject, signal } from '@angular/core';
import { UiService } from '@osee/shared/services';
import { toSignal } from '@angular/core/rxjs-interop';
import {
	tab,
	artifact,
	TabType,
	artifactSentinel,
	fetchIconFromDictionary,
} from '../types/artifact-explorer.data';

@Injectable({
	providedIn: 'root',
})
export class ArtifactExplorerTabService {
	private tabs = signal<tab[]>([]);
	private _selectedIndex = signal<number>(0);

	private uiService = inject(UiService);
	branchId = toSignal(this.uiService.id, { initialValue: '' });
	viewId = toSignal(this.uiService.viewId, { initialValue: '' });

	addTab(tabType: TabType, tabTitle: string) {
		this.tabs.update((rows) => [
			...rows,
			{
				tabType,
				tabTitle,
				artifact: artifactSentinel,
				branchId: this.branchId(),
				viewId: this.viewId(),
			},
		]);
		this.SelectedIndex = this.tabs().length - 1;
	}

	addArtifactTab(artifact: artifact) {
		// don't open a tab for the same artifact on the same branch
		if (
			!this.tabs().some(
				(existingTab) =>
					existingTab.branchId === this.uiService.id.value &&
					existingTab.artifact?.id === artifact.id
			)
		)
			this.tabs.update((rows) => [
				...rows,
				{
					tabType: 'Artifact',
					tabTitle: artifact.name,
					artifact: artifact,
					branchId: this.branchId(),
					viewId: this.viewId(),
				},
			]);
		this.SelectedIndex = this.tabs().length - 1;
	}

	removeTab(index: number) {
		this.tabs.update((rows) => rows.filter((_, i) => index !== i));
	}

	getTabIcon(tab: tab) {
		if (tab.tabType === 'Artifact' && tab.artifact) {
			return fetchIconFromDictionary(tab.artifact.typeName);
		} else if (tab.tabType === 'ChangeReport') {
			return 'differences';
		}
		return '';
	}

	get Tabs() {
		return this.tabs;
	}

	get selectedIndex() {
		return this._selectedIndex;
	}

	set SelectedIndex(index: number) {
		this._selectedIndex.set(index);
	}

	onTabDropped(event: CdkDragDrop<any[]>) {
		moveItemInArray(
			this.tabs(),
			parseInt(event.previousContainer.id),
			parseInt(event.container.id)
		);
	}
}
