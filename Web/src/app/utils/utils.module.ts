import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceparentfilterPipe } from './serviceparentfilter.pipe';



@NgModule({
    declarations: [
        ServiceparentfilterPipe
    ],
    exports: [
        ServiceparentfilterPipe
    ],
    imports: [
        CommonModule
    ]
})
export class UtilsModule { }
